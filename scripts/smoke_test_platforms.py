#!/usr/bin/env python3
"""Smoke test semua platform Dramaku: home -> id -> stream -> probe URL video.

Meniru alur resolve di MainActivity + ExtraPlatforms. Dipakai untuk menemukan
platform yang mati di sisi server vs rusak di sisi klien, tanpa cek manual
satu-satu di aplikasi.

Jalankan: python3 scripts/smoke_test_platforms.py [--platform id]
"""
import json
import re
import sys
import urllib.parse
import urllib.request
import urllib.error
import concurrent.futures

HUB = "https://dramahub.be"
TOKEN = "Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e"
UA = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/121 Mobile Safari/537.36"

MAIN_BASE = {
    "melolo": f"{HUB}/melolo/api/v1",
    "dramanova": f"{HUB}/dramanova",
    "freereels": f"{HUB}/freereels/api/v1",
    "dramabox": f"{HUB}/dramaboxv2/api/v1",
    "netshort": f"{HUB}/netshort/api/v1",
    "reelshort": f"{HUB}/reelshort/api/v1",
    "stardusttv": f"{HUB}/stardusttv/api/v1",
}
EXTRA = [
    "bonustv", "cashdrama", "dotdrama", "dramabite", "dramapops", "dramarush",
    "flareflow", "flextv", "flickreels", "goodshort", "happyshort", "idrama",
    "luminareels", "meloshort", "microdrama", "minishort", "moboreels", "playlet",
    "radreels", "rapidtv", "raptdrama", "reelbuzz", "sereal", "shortbox",
    "shorten", "shortmax", "shortsky", "shortwave", "shotshort", "snackshort",
    "sodareels", "starshort", "storyreel", "vibeshort", "wetv",
]

def base(p):
    return MAIN_BASE.get(p, f"{HUB}/{p}")

def http(url, is_json=True, send_auth=None, rng=None, timeout=25):
    """Return (status, body-bytes|json, final_url). send_auth default: only dramahub."""
    if send_auth is None:
        send_auth = "dramahub.be" in url
    req = urllib.request.Request(url, headers={
        "User-Agent": UA,
        "Accept": "application/json, text/plain, */*" if is_json else "*/*",
    })
    if send_auth:
        req.add_header("Authorization", TOKEN)
    if rng:
        req.add_header("Range", rng)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as r:
            body = r.read(1024 if rng else 5 * 1024 * 1024)
            return r.status, body, r.geturl()
    except urllib.error.HTTPError as e:
        return e.code, e.read(4096) if e.fp else b"", url
    except Exception as e:
        return -1, str(e).encode(), url

def get_json(url, **kw):
    code, body, _ = http(url, **kw)
    if code != 200:
        raise RuntimeError(f"HTTP {code} {url.split('?')[0][-60:]}")
    try:
        return json.loads(body.decode("utf-8", "replace"))
    except Exception:
        raise RuntimeError(f"non-JSON {url.split('?')[0][-60:]}")

ID_KEYS = ("book_id", "bookId", "video_id", "videoId", "drama_id", "dramaId",
           "series_id", "seriesId", "shortPlayId", "short_play_code", "vid",
           "cid", "contentId", "slug", "code", "key", "id")
COVER_KEYS = ("cover", "poster", "book_pic", "bookPic", "coverWap", "cover_url",
              "coverUrl", "vertical_cover", "verticalCover", "thumb", "image",
              "img", "pic", "icon", "coverImg", "videoCover", "shortPlayCover")
TITLE_KEYS = ("title", "book_name", "bookName", "name", "drama_name", "dramaName",
              "videoName", "video_name", "seriesName", "shortPlayName", "subject")
URL_RE = re.compile(r"https?://[^\s\"']+(?:\.(?:m3u8|mp4)(?:[?#][^\s\"']*)?|/api/web/stream\?u=[A-Za-z0-9_=-]+)", re.I)

def walk(o):
    if isinstance(o, dict):
        yield o
        for v in o.values():
            yield from walk(v)
    elif isinstance(o, list):
        for v in o:
            yield from walk(v)

def first_drama(js, platform=None):
    fallback = (None, None)
    for obj in walk(js):
        # DramaBox v2: buku prefix 41… mati di upstream — lewati seperti aplikasi.
        if platform == "dramabox" and str(obj.get("bookId", "")).startswith("41"):
            continue
        t = next((str(obj[k]).strip() for k in TITLE_KEYS if obj.get(k)), "")
        i = next((str(obj[k]).strip() for k in ID_KEYS if obj.get(k) not in (None, "", 0)), "")
        if not (t and i) or t.startswith("http"):
            continue
        has_cover = any(str(obj.get(k, "")).startswith("http") for k in COVER_KEYS)
        if has_cover:
            return i, t
        if fallback[0] is None:
            fallback = (i, t)
    return fallback

def find_media_url(js):
    text = json.dumps(js)
    m = URL_RE.search(text.replace("\\/", "/"))
    return m.group(0) if m else ""

def probe(url, referer=None):
    """Cek link video hidup: Range GET tanpa Authorization (kecuali dramahub)."""
    if not url.startswith("http"):
        return False, "URL kosong"
    req = urllib.request.Request(url, headers={"User-Agent": UA, "Accept": "*/*",
                                               "Range": "bytes=0-255"})
    if referer:
        req.add_header("Referer", referer)
    if "dramahub.be" in url:
        req.add_header("Authorization", TOKEN)
    try:
        with urllib.request.urlopen(req, timeout=20) as r:
            body = r.read(512)
            code = r.status
    except urllib.error.HTTPError as e:
        code, body = e.code, b""
    except Exception as e:
        return False, str(e)[:40]
    if code in (200, 206):
        if ".m3u8" in url.split("?")[0] and b"#EXT" not in body and b"<" in body[:20]:
            return False, f"bukan playlist (HTTP {code})"
        return True, f"HTTP {code}" + (f" ref={referer.split('//')[1][:18]}" if referer else "")
    return False, f"HTTP {code}"

def probe_any(url):
    """Coba tanpa referer, lalu dengan referer cdreader (pola app sekarang)."""
    ok, why = probe(url)
    if ok:
        return ok, why + " tanpa-referer"
    ok2, why2 = probe(url, referer="https://www.cdreader.com/")
    if ok2:
        return ok2, why2
    return False, why

def probe_with_auth(url):
    code, _, _ = http(url, is_json=False, send_auth=True, rng="bytes=0-255", timeout=20)
    return code

HOME = {
    "bonustv": "{b}/api/modules?lang=id",
    "cashdrama": "{b}/api/v1/home?lang=id&page=1&pageSize=20&blockId=5",
    "dotdrama": "{b}/api/v1/dramas?page=1&limit=50&lang=id",
    "dramabite": "{b}/api/v1/foryou?lang=id&page=1",
    "dramapops": "{b}/api/drama/popular?limit=20&language=en",
    "dramarush": "{b}/api/v1/tabs/0?lang=id",
    "flareflow": "{b}/api/popular/list?language=en",
    "flextv": "{b}/api/v1/tabs/1?lang=id&page=1",
    "flickreels": "{b}/api/v1/for-you?page=1&page_size=20&lang=id",
    "goodshort": "{b}/api/v1/home?channelId=656&page=1&pageSize=20&index=1",
    "happyshort": "{b}/home?lang=en",
    "idrama": "{b}/api/v1/popular?page=1&limit=20&lang=id",
    "luminareels": "{b}/api/drama/list?page=1&page_size=20&lang=id",
    "meloshort": "{b}/api/v1/dramas/top?lang=id",
    "microdrama": "{b}/api/v1/dramas?lang=id&limit=50",
    "minishort": "{b}/api/home_recommend?language=en",
    "moboreels": "{b}/api/hotList?listId=10&skipSeries=1&langId=3",
    "playlet": "{b}/shorts/recommend?language=en&page=1&pageSize=20",
    "radreels": "{b}/api/v1/ranking?lang=id",
    "rapidtv": "{b}/api/v1/dramas?page=1&size=20&lang=id",
    "raptdrama": "{b}/open/classify_video?type=20&page=1&limit=10&lang=en-US",
    "reelbuzz": "{b}/api/drama/module/recommend?pageNum=1&pageSize=20&language=id",
    "sereal": "{b}/api/index/home/1?lang=id",
    "shortbox": "{b}/api/list?page=1&page_size=20&languages=id&streamable=true",
    "shorten": "{b}/api/v1/editors?page=1&perPage=20",
    "shortmax": "{b}/api/v1/feed/recommend?lang=id",
    "shortsky": "{b}/api/home?lang=id_id",
    "shortwave": "{b}/api/top?lang=id",
    "shotshort": "{b}/api/popular?page=1&lang=id",
    "snackshort": "{b}/api/v1/home?lang=Indonesian",
    "sodareels": "{b}/api/v1/dramas?page=1&limit=20&lang=id",
    "starshort": "{b}/api/v1/dramas?lang=4",
    "storyreel": "{b}/api/popular?page=1&page_size=20&lang=in",
    "vibeshort": "{b}/api/home/ranking?lang=en",
    "wetv": "{b}/api/search?keyword=cinta&lang=id&country=ID",
    # platform utama
    "melolo": "{b}/bookmall?lang=id",
    "dramanova": "{b}/discover/feed?pageNum=1&pageSize=20&languages=in",
    "freereels": "{b}/foryou?page=1&lang=id-ID",
    "dramabox": "{b}/home?lang=in&page=1",
    "netshort": "{b}/explore/1?lang=id_ID",
    "reelshort": "{b}/foryou?lang=in",
    "stardusttv": "{b}/homepage?lang=id",
}

STREAM = {
    "bonustv": "{b}/api/stream/{id}?video_id={id}&episode_num=1&resolution=720p&lang=id",
    "cashdrama": "{b}/api/v1/play/{id}/1?lang=id",
    "dramabite": "{b}/api/v1/drama/{id}/episode/1?lang=id",
    "dramapops": "{b}/api/drama/{id}/1?quality=720p&language=en",
    "dramarush": "{b}/api/v1/play/{id}/1?lang=id",
    "flareflow": "{b}/api/drama/{id}/play?ep=1&language=en",
    "goodshort": "{b}/api/v1/unlock/{id}?q=720p",
    "happyshort": "{b}/drama/{id}/play/1",
    "idrama": "{b}/api/v1/drama/{id}/play?episode=1&lang=id",
    "moboreels": "{b}/api/video?seriesId={id}&episNum=1&langId=3",
    "reelbuzz": "{b}/api/drama/video/{id}/1?language=id",
    "sereal": "{b}/api/watch/{id}/1?lang=id",
    "shortbox": "{b}/api/stream/{id}/1?quality=720",
    "shortmax": "{b}/api/v1/play/{id}?ep=1&lang=id",
    "shortsky": "{b}/api/drama/{id}/episode/1?lang=id_id",
    "starshort": "{b}/api/v1/dramas/{id}/episodes/1?lang=4",
    "storyreel": "{b}/api/play?short_play_code={id}&episode_num=1&lang=in",
    "vibeshort": "{b}/api/unlock/farm/{id}/1",
    # platform utama
    "stardusttv": "{b}/video/{id}/episode/1?lang=id",
    "dramanova": "{b}/unlock/{id}/1?languages=in&subtitles=true",
    "freereels": "{b}/dramas/{id}/play/1?lang=id-ID",
    "dramabox": "{b}/play?bookId={id}&episode=1&lang=in",
    "netshort": "{b}/episode/{id}/1?lang=id_ID",
    "melolo": "{b}/multi-video?id={id}&lang=id",
}
# Platform dengan URL video tertanam di detail (token pendek)
DETAIL_EMBEDDED = {"sodareels": "{b}/api/v1/info/{id}", "dotdrama": "{b}/api/v1/dramas/{id}",
                   "shorten": "{b}/api/v1/series/{id}", "raptdrama": "{b}/video/playlist?vid={id}&lang=en-US",
                   "rapidtv": "{b}/api/v1/dramas/{id}?lang=id", "microdrama": "{b}/api/v1/dramas/{id}"}
# Platform dengan chapter-id: ambil daftar dulu, baru stream
CHAPTERED = {
    "flextv": ("{b}/api/v1/series/{id}/episodes?lang=id", "{b}/api/v1/play/{id}/{ch}?lang=id"),
    "flickreels": ("{b}/api/v1/chapters/{id}?lang=id", "{b}/api/v1/stream/{id}/{ch}?lang=id"),
    "meloshort": ("{b}/api/v1/dramas/{id}/episodes?page=1&limit=100&lang=id", "{b}/api/v1/dramas/{id}/episodes/{ch}?lang=id"),
    "minishort": ("{b}/api/video_info?video_id={id}&language=en", "{b}/api/episodes_content?video_id={id}&episodes_id={ch}&language=en"),
    "radreels": ("{b}/api/v1/episodes/{id}?lang=id", "{b}/api/v1/video/{ch}?lang=id"),
    "shotshort": ("{b}/api/book/{id}/episodes?lang=id", "{b}/api/book/{id}/chapter/{ch}?lang=id"),
    "shortwave": ("{b}/api/drama/{id}?lang=id", "{b}/api/stream/{id}/{ch}?lang=id"),
    "snackshort": ("{b}/api/v1/book/{id}/chapters?lang=Indonesian", "{b}/api/v1/book/{id}/episode/{ch}?lang=Indonesian"),
    "wetv": ("{b}/api/episodes?cid={id}&lang=id&country=ID", "{b}/api/play?vid={ch}&cid={id}&lang=id&country=ID"),
    "reelshort": ("{b}/book/{id}/chapters?lang=in", "{b}/book/{id}/chapter/{ch}/video"),
}
CHAPTER_KEYS = ("chapter_id", "chapterId", "episodes_id", "episodeId", "vid", "id")

def chapter_of(js, platform):
    objs = [o for o in walk(js)]
    if platform == "radreels":
        for o in objs:
            if o.get("videoFakeId") and o.get("id"):
                return f"{o['videoFakeId']}/{o['id']}"
        return None
    for o in objs:
        num = o.get("chapter_num") or o.get("episode_num") or o.get("episodeNo") or o.get("sort") or o.get("num")
        has_ch = any(o.get(k) for k in CHAPTER_KEYS)
        if has_ch and (num in (1, "1") or num is None):
            for k in CHAPTER_KEYS:
                if o.get(k):
                    return str(o[k])
    return None

def test_platform(p):
    b = base(p)
    try:
        home = get_json(HOME[p].format(b=b))
    except Exception as e:
        return p, "HOME-FAIL", str(e)[:80]
    did, title = first_drama(home, p)
    if not did:
        return p, "HOME-EMPTY", "tidak menemukan drama di balasan home"
    eid = urllib.parse.quote(did, safe="")
    try:
        if p in DETAIL_EMBEDDED:
            js = get_json(DETAIL_EMBEDDED[p].format(b=b, id=eid))
        elif p in CHAPTERED:
            list_url, stream_url = CHAPTERED[p]
            ch = chapter_of(get_json(list_url.format(b=b, id=eid)), p)
            if not ch:
                return p, "CHAPTER-FAIL", f"id={did[:20]} tidak dapat chapter"
            js = get_json(stream_url.format(b=b, id=eid, ch=urllib.parse.quote(str(ch), safe="/")))
        elif p == "playlet":
            url = f"{b}/theater/{eid}/open/1/playlist.m3u8"
            ok, why = probe_any(url)
            return p, "OK" if ok else "VIDEO-DEAD", f"{why} [{title[:28]}]"
        elif p == "luminareels":
            url = f"{b}/api/stream/{eid}/1/master.m3u8?lang=id"
            ok, why = probe_any(url)
            return p, "OK" if ok else "VIDEO-DEAD", f"{why} [{title[:28]}]"
        else:
            js = get_json(STREAM[p].format(b=b, id=eid))
    except Exception as e:
        return p, "STREAM-FAIL", f"{str(e)[:70]} [{title[:24]}]"
    murl = find_media_url(js)
    if not murl:
        keys = sorted({k for o in walk(js) for k in o})[:18]
        return p, "NO-URL", f"balasan tanpa m3u8/mp4 [{title[:24]}] keys={','.join(keys)[:90]}"
    ok, why = probe_any(murl)
    if ok:
        return p, "OK", f"{why} [{title[:28]}]"
    # Bandingkan: apakah mengirim Authorization justru merusak?
    auth_code = probe_with_auth(murl)
    return p, "VIDEO-DEAD", f"{why} (dgn auth: {auth_code}) [{title[:26]}] {murl[:60]}"

def main():
    targets = sys.argv[2:] if len(sys.argv) > 2 and sys.argv[1] == "--platform" else (
        list(MAIN_BASE.keys()) + EXTRA)
    results = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=8) as ex:
        for r in ex.map(test_platform, targets):
            results.append(r)
            print(f"{r[0]:<12} {r[1]:<12} {r[2]}", flush=True)
    ok = sum(1 for r in results if r[1] == "OK")
    print(f"\n== {ok}/{len(results)} platform OK ==")
    for r in results:
        if r[1] != "OK":
            print(f"  BROKEN: {r[0]:<12} {r[1]:<12} {r[2]}")

if __name__ == "__main__":
    main()
