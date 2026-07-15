/* Dramaku video player */
function streamFailHtml(did,ep,msg){
  return `<div class="stream-fail"><p>${esc(msg||'Video belum tersedia')}</p><button type="button" class="retry" onclick="retryStream('${jsStr(did)}',${Number(ep)||1})">Coba Lagi</button></div>`;
}
function retryStream(did,ep){
  const id=String(did||'');
  let slide=null;
  document.querySelectorAll(`.v-slide[data-ep="${Number(ep)||1}"]`).forEach(s=>{if(String(s.dataset.did)===id)slide=s});
  if(slide){
    delete slide.dataset.ld;
    try{if(slide._hls){slide._hls.destroy();slide._hls=null}}catch(e){}
    slide.querySelectorAll('video').forEach(v=>{try{v.pause()}catch(e){}v.remove()});
    const loader=slide.querySelector('.v-loading');
    if(loader){loader.style.display='flex';loader.innerHTML='<div class="spinner"></div><span style="color:var(--text3);font-size:10px">Memuat ulang...</span>'}
  }
  loadVid(did,ep);
}
function applyFitMode(){const ov=$('#plOv');if(!ov)return;ov.classList.toggle('fit-contain',fitMode==='contain');ov.classList.toggle('fit-cover',fitMode!=='contain');$$('.fit-label',ov).forEach(e=>e.textContent=fitMode==='contain'?'Asli':'Full')}
function toggleFitMode(){setFitMode(fitMode==='contain'?'cover':'contain');toast(fitMode==='cover'?'Video dibuat full layar':'Video ditampilkan ukuran asli')}
function bindSeekBar(slide,vid,did,ep){
  const prog=slide.querySelector('.v-prog'),bar=slide.querySelector('.v-prog-bar');if(!prog||!bar)return;
  let seeking=false,hideTipTimer=null;
  const tip=(()=>{let el=slide.querySelector('.seek-tip');if(!el){el=document.createElement('div');el.className='seek-tip';slide.appendChild(el)}return el})();
  const setByX=(x,commit)=>{
    if(!vid.duration||!isFinite(vid.duration))return;
    const r=prog.getBoundingClientRect();
    const pct=Math.max(0,Math.min(1,(x-r.left)/Math.max(1,r.width)));
    const time=pct*vid.duration;
    try{vid.currentTime=time}catch(e){}
    bar.style.width=(pct*100)+'%';
    tip.textContent=fmtTime(time)+' / '+fmtTime(vid.duration);
    tip.style.left=(pct*100)+'%';
    clearTimeout(hideTipTimer);tip.classList.add('on');
    if(commit)saveWatchProgress(did,ep,time,vid.duration);
  };
  const start=e=>{e.preventDefault();e.stopPropagation();seeking=true;prog.classList.add('seeking');prog.dataset.wasPlaying=vid.paused?'0':'1';try{prog.setPointerCapture(e.pointerId)}catch(_){};vid.pause();setByX(e.clientX,false)};
  const move=e=>{if(!seeking)return;e.preventDefault();e.stopPropagation();setByX(e.clientX,false)};
  const end=e=>{if(!seeking)return;e.preventDefault();e.stopPropagation();setByX(e.clientX,true);seeking=false;prog.classList.remove('seeking');try{prog.releasePointerCapture(e.pointerId)}catch(_){};if(prog.dataset.wasPlaying==='1')vid.play().catch(()=>{});hideTipTimer=setTimeout(()=>tip.classList.remove('on'),900)};
  prog.addEventListener('pointerdown',start,{passive:false});
  prog.addEventListener('pointermove',move,{passive:false});
  prog.addEventListener('pointerup',end,{passive:false});
  prog.addEventListener('pointercancel',end,{passive:false});
  prog.addEventListener('click',e=>{e.preventDefault();e.stopPropagation()});
}
function showPlayerHint(){try{if(localStorage.getItem('dk_player_hint_seen'))return;localStorage.setItem('dk_player_hint_seen','1')}catch(e){}const ov=$('#plOv');if(!ov)return;const h=document.createElement('div');h.className='player-hint';h.innerHTML='<div><b>Tips Player</b><span>Double tap kiri/kanan: mundur/maju 10 detik</span><span>Tahan video: speed 2x sementara</span><span>Scroll atas/bawah: pindah episode</span><button>Mengerti</button></div>';ov.appendChild(h);h.querySelector('button').onclick=()=>h.remove();setTimeout(()=>h.remove(),6500)}
function unloadSlideMedia(s){try{if(s._hls){s._hls.destroy();s._hls=null}const v=s.querySelector('video');if(v){v.pause();v.removeAttribute('src');v.load();v.remove()}delete s.dataset.ld;const loader=s.querySelector('.v-loading');if(loader){loader.style.display='flex';loader.innerHTML='<div class="spinner"></div><span style="color:var(--text3);font-size:10px">Episode '+s.dataset.ep+'</span>'}}catch(e){}}
function trimPlayerMedia(cont,activeEp){if(!isPerformanceMode())return;cont.querySelectorAll('.v-slide').forEach(s=>{const e=+s.dataset.ep;if(Math.abs(e-activeEp)>1)unloadSlideMedia(s)})}
async function play(did,ep,opts={}){
  const clipOnly=!!opts.clip;clipPreviewMode=clipOnly;
  closeEpModal();setNativePlayback(true);$('#plOv').classList.add('on');applyFitMode();showPlayerHint();$('#plName').textContent=curDrama?.drama_name||'';$('#plEp').textContent=(clipOnly?'Cuplikan · ':'')+'Episode '+ep;
  if(curDrama&&!clipOnly)saveHistory(curDrama,ep);
  curPE=ep;const cont=$('#plCont');cont.innerHTML='';const total=curDrama?.episode_count||curEps.length||ep;
  const back=clipOnly?0:(isPerformanceMode()?0:2),forward=clipOnly?1:(isPerformanceMode()?2:3);const ps=Math.max(1,ep-back);for(let i=ps;i<ep;i++)cont.insertAdjacentHTML('beforeend',slideHtml(did,i));
  for(let i=ep;i<ep+Math.min(forward,total-ep+1);i++)cont.insertAdjacentHTML('beforeend',slideHtml(did,i));
  loadVid(did,ep);
  requestAnimationFrame(()=>{const t=cont.querySelector(`.v-slide[data-ep="${ep}"]`);if(t)t.scrollIntoView({behavior:'instant'})});
  cont.onscroll=debounce(()=>{
    const slides=cont.querySelectorAll('.v-slide');let active=null;
    slides.forEach(s=>{const r=s.getBoundingClientRect();if(r.top>=-80&&r.top<window.innerHeight/2)active=s});
    if(!active)return;const ae=+active.dataset.ep;$('#plEp').textContent=(clipPreviewMode?'Cuplikan · ':'Episode ')+ (clipPreviewMode?'Episode '+ae:ae);curPE=ae;showUI();
    if(clipPreviewMode)return;
    if(curDrama)saveHistory(curDrama,ae);
    cont.querySelectorAll('video').forEach(v=>{if(v.closest('.v-slide')===active)v.play().catch(()=>{});else v.pause()});
    if(!active.dataset.ld)loadVid(did,ae);
    const last=slides[slides.length-1],le=+last.dataset.ep;
    const add=isPerformanceMode()?1:2;if(ae>=le-1&&le<total)for(let i=le+1;i<=Math.min(le+add,total);i++)cont.insertAdjacentHTML('beforeend',slideHtml(did,i));
    const first=slides[0],fe=+first.dataset.ep;
    if(!isPerformanceMode()&&ae<=fe+1&&fe>1)for(let i=fe-1;i>=Math.max(1,fe-2);i--)cont.insertAdjacentHTML('afterbegin',slideHtml(did,i));
    trimPlayerMedia(cont,ae);
  },120);
}
function flashGesture(slide,text,side='center'){
  let el=slide.querySelector('.gesture-flash');
  if(!el){el=document.createElement('div');el.className='gesture-flash';slide.appendChild(el)}
  el.className='gesture-flash '+side;el.textContent=text;el.classList.remove('show');void el.offsetWidth;el.classList.add('show');
  clearTimeout(el._tm);el._tm=setTimeout(()=>el.classList.remove('show'),650);
}
function showSpeedBadge(slide,on){let el=slide.querySelector('.speed-badge');if(!el){el=document.createElement('div');el.className='speed-badge';el.textContent='2x';slide.appendChild(el)}el.classList.toggle('show',!!on)}
function seekBy(vid,slide,delta){if(!vid.duration||!isFinite(vid.duration))return;const next=Math.max(0,Math.min(vid.duration,vid.currentTime+delta));try{vid.currentTime=next}catch(e){}const p=slide.querySelector('.v-prog-bar');if(p)p.style.width=(next/vid.duration*100)+'%';flashGesture(slide,(delta>0?'+':'')+delta+' detik',delta>0?'right':'left')}
function slideHtml(did,ep){
  const nm=esc(curDrama?.drama_name||''),ds=esc(curDrama?.description||'');
  return`<div class="v-slide" data-ep="${ep}" data-did="${esc(did)}"><div class="v-loading"><div class="spinner"></div><span style="color:var(--text3);font-size:10px">Episode ${ep}</span></div>
  <div class="v-info"><div class="ep-lbl">${clipPreviewMode?'Cuplikan':'Episode'} ${ep}</div><div class="ep-nm">${nm}</div><div class="ep-ds">${ds}</div>${clipPreviewMode?'<button class="clip-full-watch" onclick="playFullFromClip()">Tonton Semua Episode</button>':''}</div>
  <div class="v-actions"><div class="v-act"><button onclick="this.classList.toggle('liked')" aria-label="Suka"><svg width="19" height="19" viewBox="0 0 24 24" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54z"/></svg></button><span>Suka</span></div>
  <div class="v-act"><button onclick="openEpModal()" aria-label="Daftar episode"><svg width="19" height="19" viewBox="0 0 24 24" fill="currentColor"><path d="M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z"/></svg></button><span>Episode</span></div>
  <div class="v-act"><button onclick="toggleFitMode()" aria-label="Ubah ukuran video"><svg width="19" height="19" viewBox="0 0 24 24" fill="currentColor"><path d="M7 14H5v5h5v-2H7v-3zm-2-4h2V7h3V5H5v5zm12 7h-3v2h5v-5h-2v3zM14 5v2h3v3h2V5h-5z"/></svg></button><span class="fit-label">${fitMode==='contain'?'Asli':'Full'}</span></div>
  <div class="v-act"><button onclick="reportEpisode(this)" aria-label="Laporkan episode"><svg width="19" height="19" viewBox="0 0 24 24" fill="currentColor"><path d="M14.4 6l-.24-1.2A1 1 0 0013.18 4H5v17h2v-7h5.6l.24 1.2a1 1 0 00.98.8H20V6h-5.6z"/></svg></button><span class="report-dot">Lapor</span></div></div>
  <div class="dbl-heart" id="heart-${ep}"><svg width="80" height="80" viewBox="0 0 24 24" fill="var(--accent)"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54z"/></svg></div>
  <div class="v-prog"><div class="v-prog-bar" id="prg-${ep}"></div></div></div>`;
}
async function loadVid(did,ep){
  const slide=$(`.v-slide[data-ep="${ep}"][data-did="${did}"]`);if(!slide||slide.dataset.ld)return;slide.dataset.ld='1';
  const loader=slide.querySelector('.v-loading');
  let url=null,isHLS=false,subUrl=null;const dp=curDrama?._p||platCache[did]||P;
  if(dp==='freereels'){try{const d=await fetchJsonTimeout(API.freereels+`/stream?dramaId=${did}&episode=${ep}&lang=id`);if(d?.data){let raw=d.data.h264_m3u8||d.data.m3u8_url||d.data.video_url;if(raw){url='https://proxy.sonzaixlab.workers.dev/proxy?url='+encodeURIComponent(raw);isHLS=true};if(d.data.subtitles?.length){const st=d.data.subtitles.find(s=>s.language==='id-ID'||s.language?.startsWith('id'))||d.data.subtitles[0];if(st?.url)subUrl=st.url}}}catch(e){}}
  else if(dp==='flickreels'){try{const d=await fetchJsonTimeout(API.flickreels+`/stream?id=${did}&ep=${ep}`);if(d?.data?.hls_url){url=d.data.hls_url;isHLS=true}}catch(e){}}
  else if(dp==='reelshort'){try{const d=await fetchJsonTimeout(API.reelshort+`/stream?id=${did}&episode_no=${ep}`);if(d?.data){const vl=d.data.videoList||[];const pick=vl.find(v=>v.encode==='H264'&&v.dpi===720)||vl.find(v=>v.encode==='H264')||vl[0];if(pick?.playUrl){url=pick.playUrl;isHLS=true}else if(d.data.play_url){url=d.data.play_url;isHLS=true}}}catch(e){}}
  else if(dp==='drakor'){
    try{const epData=(curEps||[]).find(e=>Number(e.episode_number)===Number(ep))||(curEps||[])[ep-1];const sid=epData?.streaming;if(sid){const d=await fetchJsonTimeout(API.drakor+`/stream?streaming=${sid}`);url=(Settings.get().dataSaver?(d['480p']||d['360p']||d['720p']):(d['720p']||d['480p']||d['360p']));isHLS=!!url}}catch(e){}
  }else if(dp==='moviebox'){
    try{const st=curDrama?._subjectType||1;let d,mvUrl='',mvSub='';
      if(st===2){d=await fetchJsonTimeout(API.moviebox+`/download-series?subjectId=${did}&se=1&resolution=${Settings.get().dataSaver?480:720}`);if(d?.data?.episodes?.length){const epd=d.data.episodes.find(e=>e.ep===ep)||d.data.episodes[0];if(epd?.resourceLink)mvUrl=epd.resourceLink;if(epd?.subtitle?.url)mvSub=epd.subtitle.url}}
      else{d=await fetchJsonTimeout(API.moviebox+`/download-movie?subjectId=${did}&resolution=${Settings.get().dataSaver?480:720}`);if(d?.data?.files?.length){const f=d.data.files.find(f=>f.codecName==='h264')||d.data.files[0];if(f?.resourceLink)mvUrl=f.resourceLink};if(d?.data?.subtitle?.url)mvSub=d.data.subtitle.url}
      if(mvUrl){
        // Coba native player (ExoPlayer di APK) - support HEVC
        if(typeof NativePlayer!=='undefined'&&(NativePlayer.playFull||NativePlayer.play)){
          const startPos=Math.floor((getWatchProgress(did,ep)?.pos||0)*1000);
          const title=cleanText(curDrama?.drama_name||'Dramaku')+' · Ep '+ep;
          try{
            if(NativePlayer.playFull){
              NativePlayer.playFull(mvUrl,mvSub||'',title,String(did||''),Number(ep)||1,String(dp||''),startPos);
            }else{
              NativePlayer.play(mvUrl,mvSub||'',title);
            }
          }catch(e){ErrorLog.capture('native_player',String(e?.message||e));}
          if(loader)loader.innerHTML='<p style="color:var(--accent);font-size:11px">Membuka player native...</p>';
          if(curDrama)saveHistory(curDrama,ep);
          return;
        }
        // Fallback: browser video element (hanya h264)
        url=mvUrl;subUrl=mvSub;
      }
    }catch(e){}
  }else if(dp==='goodshort'){
    try{
      // GoodShort stream returns all episodes. Cache it.
      if(!window._gsCache||window._gsCache.id!==did){const d=await fetchJsonTimeout(API.goodshort+`/stream?bookId=${did}`);if(d?.data?.downloadList)window._gsCache={id:did,list:d.data.downloadList}}
      if(window._gsCache?.list){const epData=window._gsCache.list[ep-1];if(epData?.multiVideos?.length){const mv=epData.multiVideos.find(v=>v.type===(Settings.get().dataSaver?'480p':'720p'))||epData.multiVideos.find(v=>v.type==='720p')||epData.multiVideos[0];if(mv?.filePath){url=mv.filePath;isHLS=true}}}
    }catch(e){}
  }else if(dp==='dramabox'){
    try{const d=await fetchJsonTimeout(API.dramabox+`/stream?bookId=${did}&chapterIndex=${ep-1}&lang=in`);if(d?.data){url=d.data.videoUrl||null;if(!url&&d.data.qualities?.length){const q=d.data.qualities.find(q=>q.quality===720)||d.data.qualities[0];if(q)url=q.videoPath}}}catch(e){}
  }else if(dp==='netshort'){
    try{const d=await fetchJsonTimeout(API.netshort+`/streamv2?id=${did}&ep=${ep}`);if(d?.data){url=d.data.play_url||null;if(!url&&d.data.streams?.length){const s=d.data.streams.find(s=>s.encode==='H264')||d.data.streams[0];if(s)url=s.url}}}catch(e){}
  }else if(dp==='dramanova'){try{const d=await fetchJsonTimeout(API.dramanova+`/stream?id=${did}&ep=${ep}`);if(d?.data?.play){url=d.data.play.video_url||d.data.play.backup_url||null;if(!url&&d.data.play.qualities?.length){const q=d.data.play.qualities.find(q=>q.codec==='h264')||d.data.play.qualities[0];url=q.main_url||q.backup_url}};if(d?.data?.info?.subtitle_tracks?.length){const st=d.data.info.subtitle_tracks.find(s=>s.language==='in'||s.language==='id')||d.data.info.subtitle_tracks[0];if(st?.label)subUrl=st.label}}catch(e){}}
  else{try{const d=await fetchJsonTimeout(API.melolo+`/streamv2?id=${did}&ep=${ep}`);if(d?.url&&d.playable!==false)url=d.url}catch(e){}
    if(!url){try{const d=await fetchJsonTimeout(API.melolo+`/stream?id=${did}&ep=${ep}`);if(d?.qualities?.length){const q=d.qualities.find(q=>q.codec==='h264')||d.qualities[d.qualities.length-1];if(q)url=q.url}}catch(e){}}}
  if(!url){if(loader){loader.style.display='flex';loader.innerHTML=streamFailHtml(did,ep,'Video belum tersedia');}slide.dataset.ld='';ErrorLog.capture('stream','Video belum tersedia',{platform:dp,id:did,ep});return}
  const vid=document.createElement('video');
  vid.setAttribute('playsinline','');vid.setAttribute('webkit-playsinline','');vid.setAttribute('preload','auto');
  if(isHLS&&typeof Hls!=='undefined'&&Hls.isSupported()){const hls=new Hls({maxBufferLength:30,enableWorker:true});hls.loadSource(url);hls.attachMedia(vid);hls.on(Hls.Events.ERROR,(e,d)=>{if(d.fatal){slide.dataset.ld='';if(loader){loader.style.display='flex';loader.innerHTML=streamFailHtml(did,ep,'Gagal memutar stream')}}});slide._hls=hls}
  else if(isHLS&&vid.canPlayType('application/vnd.apple.mpegurl')){vid.src=url}else{vid.src=url}
  if(subUrl)loadSub(vid,subUrl);
  vid.addEventListener('loadedmetadata',()=>{const pr=getWatchProgress(did,ep);if(pr&&pr.pos>8&&vid.duration&&pr.pos<vid.duration-8){try{vid.currentTime=pr.pos}catch(e){}}});
  vid.addEventListener('loadeddata',()=>{if(loader)loader.style.display='none';if(+slide.dataset.ep===curPE){vid.play().catch(()=>{});scheduleHideUI()}});
  vid.addEventListener('timeupdate',()=>{const p=$('#prg-'+ep);if(p&&vid.duration)p.style.width=(vid.currentTime/vid.duration*100)+'%';if(vid.duration&&Date.now()-(vid._lastSave||0)>2200){saveWatchProgress(did,ep,vid.currentTime,vid.duration);vid._lastSave=Date.now()}});
  bindSeekBar(slide,vid,did,ep);
  vid.addEventListener('ended',()=>{if(clipPreviewMode){flashGesture(slide,'Cuplikan selesai','center');showUI();return}clearWatchProgress(did,ep);const ns=slide.nextElementSibling;if(Settings.get().autoNext&&ns)ns.scrollIntoView({behavior:'smooth'})});
  vid.addEventListener('error',()=>{slide.dataset.ld='';if(loader){loader.style.display='flex';loader.innerHTML=streamFailHtml(did,ep,dp==='moviebox'?'Format video tidak didukung di WebView':'Gagal memutar video')}});
  // Tap controls: single tap play/pause, double tap left/right seek, double tap center like, long press 2x speed.
  let lastTap=0,suppressClickUntil=0,longPressTimer=null,speedHold=false;
  vid.addEventListener('pointerdown',()=>{
    clearTimeout(longPressTimer);speedHold=false;
    longPressTimer=setTimeout(()=>{speedHold=true;suppressClickUntil=Date.now()+700;try{vid.playbackRate=2}catch(_){}showSpeedBadge(slide,true);flashGesture(slide,'Tahan 2x','center');if(vid.paused)vid.play().catch(()=>{})},520);
  });
  ['pointerup','pointercancel','pointerleave'].forEach(ev=>vid.addEventListener(ev,()=>{clearTimeout(longPressTimer);if(speedHold){try{vid.playbackRate=1}catch(_){}showSpeedBadge(slide,false);speedHold=false;suppressClickUntil=Date.now()+450}}));
  vid.addEventListener('click',e=>{
    e.preventDefault();if(Date.now()<suppressClickUntil)return;const now=Date.now();
    if(now-lastTap<300){const r=slide.getBoundingClientRect(),x=(e.clientX-r.left)/Math.max(1,r.width);if(x<.42)seekBy(vid,slide,-10);else if(x>.58)seekBy(vid,slide,10);else{const heart=$('#heart-'+slide.dataset.ep);if(heart){heart.classList.remove('pop');void heart.offsetWidth;heart.classList.add('pop')}const likeBtn=slide.querySelector('.v-act button');if(likeBtn&&!likeBtn.classList.contains('liked'))likeBtn.classList.add('liked');flashGesture(slide,'Suka','center')}haptic('light');lastTap=0;return}
    lastTap=now;
    setTimeout(()=>{if(Date.now()-lastTap<300||Date.now()<suppressClickUntil)return;const icon=slide.querySelector('.pause-icon')||mkPause(slide);if(vid.paused){vid.play().catch(()=>{});icon.innerHTML='<svg width="26" height="26" viewBox="0 0 24 24" fill="#fff"><path d="M8 5v14l11-7z"/></svg>';icon.classList.add('show');setTimeout(()=>icon.classList.remove('show'),400);scheduleHideUI()}else{if($('#plOv').classList.contains('p-ui-hidden')){showUI();return}vid.pause();icon.innerHTML='<svg width="26" height="26" viewBox="0 0 24 24" fill="#fff"><path d="M6 4h4v16H6zM14 4h4v16h-4z"/></svg>';icon.classList.add('show');showUI()}},300);
  });
  slide.insertBefore(vid,slide.firstChild);
}
async function loadSub(vid,srtUrl){try{const r=await fetchWithTimeout('https://proxy.sonzaixlab.workers.dev/proxy?url='+encodeURIComponent(srtUrl),{cache:'no-store'},12000);if(!r.ok)return;const srt=await r.text();let vtt='WEBVTT\n\n'+srt.replace(/(\d{2}:\d{2}:\d{2}),(\d{3})/g,'$1.$2');const b=new Blob([vtt],{type:'text/vtt'});const t=document.createElement('track');t.kind='subtitles';t.label='Indonesia';t.srclang='id';t.src=URL.createObjectURL(b);t.default=true;vid.appendChild(t);vid.textTracks[0].mode='showing'}catch(e){}}
function mkPause(s){const d=document.createElement('div');d.className='pause-icon';s.appendChild(d);return d}
function showUI(){$('#plOv').classList.remove('p-ui-hidden');const v=$('#plOv').querySelector(`.v-slide[data-ep="${curPE}"] video`);if(v&&!v.paused)scheduleHideUI()}
function hideUI(){$('#plOv').classList.add('p-ui-hidden')}
function scheduleHideUI(){clearTimeout(uiTimer);uiTimer=setTimeout(hideUI,3000)}
function nextEp(){const c=$('#plCont'),cur=c.querySelector(`.v-slide[data-ep="${curPE}"]`);if(cur?.nextElementSibling)cur.nextElementSibling.scrollIntoView({behavior:'smooth'})}
function playFullFromClip(){if(!curDrama)return;toast('Memutar semua episode...');play(curDrama.drama_id||curDrama.bookId,1,{clip:false})}
function closePl(){clipPreviewMode=false;setNativePlayback(false);clearTimeout(uiTimer);closeEpModal();const ov=$('#plOv');ov.classList.remove('on','p-ui-hidden');ov.querySelectorAll('.v-slide').forEach(unloadSlideMedia);$('#plCont').innerHTML=''}

function openEpModal(){
  showUI();clearTimeout(uiTimer);const total=curDrama?.episode_count||curEps.length||0;
  const poster=curDrama?._thumb||thumbCache[curDrama?.drama_id]||'';
  $('#epModPoster').src=poster;$('#epModTitle').textContent=curDrama?.drama_name||'';$('#epModSub').textContent=total+' Episode';
  const rs=30,rc=Math.ceil(total/rs);let rh='';
  for(let i=0;i<rc;i++){const s=i*rs+1,e=Math.min((i+1)*rs,total);rh+=`<button class="ep-m-range${i===0?' on':''}" onclick="switchEpRange(${i},${total})" data-ri="${i}">${s}-${e}</button>`}
  $('#epModRanges').innerHTML=rh;renderEpRange(0,total);$('#epBd').classList.add('on');requestAnimationFrame(()=>$('#epMod').classList.add('on'));
}
function renderEpRange(ri,total){const rs=30,s=ri*rs+1,e=Math.min((ri+1)*rs,total);let h='';for(let i=s;i<=e;i++){const p=i===curPE;h+=`<button class="ep-m-btn${p?' on':''}" onclick="playFromModal(${i})">${p?'<svg width="13" height="13" viewBox="0 0 24 24" fill="#fff"><path d="M3 9v6h4l5 5V4L7 9H3z"/><path d="M16.5 12c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z"/></svg>':i}</button>`}$('#epModGrid').innerHTML=h}
function switchEpRange(i,t){$$('.ep-m-range').forEach(e=>e.classList.toggle('on',+e.dataset.ri===i));renderEpRange(i,t)}
function playFromModal(ep){closeEpModal();const did=curDrama?.drama_id;if(!did)return;const cont=$('#plCont');cont.querySelectorAll('.v-slide').forEach(s=>{if(s._hls){s._hls.destroy();s._hls=null}});cont.querySelectorAll('video').forEach(v=>{v.pause();v.removeAttribute('src');v.load()});cont.innerHTML='';curPE=ep;$('#plEp').textContent='Episode '+ep;const total=curDrama?.episode_count||curEps.length||ep;const back=isPerformanceMode()?0:2,forward=isPerformanceMode()?2:3;const ps=Math.max(1,ep-back);for(let i=ps;i<ep;i++)cont.insertAdjacentHTML('beforeend',slideHtml(did,i));for(let i=ep;i<ep+Math.min(forward,total-ep+1);i++)cont.insertAdjacentHTML('beforeend',slideHtml(did,i));loadVid(did,ep);requestAnimationFrame(()=>{const t=cont.querySelector(`.v-slide[data-ep="${ep}"]`);if(t)t.scrollIntoView({behavior:'instant'})})}
function closeEpModal(){$('#epMod').classList.remove('on');$('#epBd').classList.remove('on')}

