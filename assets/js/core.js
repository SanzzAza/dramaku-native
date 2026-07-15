/* Dramaku core: state, utils, API cache, remote config, settings */
const API={melolo:'https://api.sonzaix.indevs.in/melolo',freereels:'https://api.sonzaix.indevs.in/freereels',flickreels:'https://api.sonzaix.indevs.in/flickreels',dramanova:'https://api.sonzaix.indevs.in/dramanova',reelshort:'https://api.sonzaix.indevs.in/reelshort',netshort:'https://api.sonzaix.indevs.in/netshort',dramabox:'https://api.sonzaix.indevs.in/dramabox',goodshort:'https://api.sonzaix.indevs.in/goodshort',moviebox:'https://api.sonzaix.indevs.in/moviebox',drakor:'https://api.sonzaix.indevs.in/drama'};
const PLAT_LABELS={melolo:'Melolo',freereels:'FreeReels',flickreels:'FlickReels',dramanova:'DramaNova',reelshort:'ReelShort',netshort:'NetShort',dramabox:'DramaBox',goodshort:'GoodShort',moviebox:'MovieBox',drakor:'Drakor'};
const REMOTE_CONFIG_URL='https://raw.githubusercontent.com/SanzzAza/dramaku/main/remote-config.json';
let remoteConfig=null,remoteConfigMeta={source:'default',updated:0,url:REMOTE_CONFIG_URL};
let P='melolo',curTab='home',pg={},busy={},more={},loaded={};
let curDrama=null,curEps=[],curPE=0,sto=null,clipPreviewMode=false;
let fitMode=(()=>{try{return localStorage.getItem('dk_fit_mode')||'cover'}catch(e){return 'cover'}})();
let lastSearchResults=[],lastSearchQuery='',searchFilter='all',searchSeq=0;
const APP_VERSION='4.5.2';
const thumbCache={},platCache={},itemCache={};
let allItems=[];
const jsonMemCache={};

// Splash
setTimeout(()=>{$('#splash').classList.add('hide');setTimeout(()=>$('#splash').remove(),500)},1800);

function resetState(){pg={home:1,populer:1,new:1,t4:1,t5:1,t6:1,t7:1};busy={};more={home:1,populer:1,new:1,t4:1,t5:1,t6:1,t7:1};loaded={};['home','clips','populer','new','history','fav','settings','profile','t4','t5','t6','t7'].forEach(k=>{const el=$('#v-'+k);if(el)el.innerHTML=''})}
function $(s,p){return(p||document).querySelector(s)}
function $$(s,p){return(p||document).querySelectorAll(s)}
function esc(v){return String(v??'').replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]))}
function cleanText(v){const d=document.createElement('div');d.innerHTML=String(v||'').replace(/&nbsp;/g,' ');return (d.textContent||d.innerText||'').replace(/\s+/g,' ').trim()}
function jsStr(v){return esc(String(v??'').replace(/\\/g,'\\\\').replace(/'/g,"\\'").replace(/[\r\n]+/g,' '))}
function platformLabel(p){return PLAT_LABELS[p]||p||'Dramaku'}
function normalizeTitleKey(name){
  return String(name||'').toLowerCase()
    .replace(/[^a-z0-9\u00c0-\u024f\u0400-\u04ff\u4e00-\u9fff\s]/gi,' ')
    .replace(/\b(the|a|an|season|season\s*\d+|s\d+|ep\s*\d+|episode\s*\d+|sub\s*indo|subtitle|full|hd|indo|indonesia)\b/g,' ')
    .replace(/\s+/g,' ')
    .trim();
}
function extractRealRating(d){
  if(!d||typeof d!=='object')return '';
  const candidates=[
    d.rating,d.score,d.imdb,d.imdb_rating,d.imdbRating,d.imdbRatingValue,
    d.vote_average,d.avg_rating,d.average_rating,d.mark,d.star,
    d._raw?.rating,d._raw?.score,d._raw?.imdbRatingValue,d._raw?.vote_average
  ];
  for(const c of candidates){
    if(c==null||c==='')continue;
    const s=String(c).trim();
    if(/imdb/i.test(s)){
      const m=s.match(/(\d+(?:\.\d+)?)/);
      if(m){const n=parseFloat(m[1]);if(n>0&&n<=10)return n.toFixed(1)}
      continue;
    }
    const n=parseFloat(s.replace(',','.'));
    if(!isNaN(n)&&n>0&&n<=10)return n.toFixed(1);
  }
  // watch_value sometimes stores "IMDb 7.2"
  const w=String(d.watch_value||'');
  if(/imdb/i.test(w)){
    const m=w.match(/(\d+(?:\.\d+)?)/);
    if(m){const n=parseFloat(m[1]);if(n>0&&n<=10)return n.toFixed(1)}
  }
  return '';
}
function ratingLabel(d,fallbackSeed){
  // Real rating only. No fake hash scores.
  if(typeof d==='object'&&d){
    const real=extractRealRating(d);
    if(real)return real;
    return '';
  }
  return '';
}
function ratingBadgeHtml(d){
  const r=ratingLabel(d);
  if(!r)return '';
  return `<div class="card-rating"><svg width="9" height="9" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3 7h7l-5.5 4.5 2 7L12 16l-6.5 4.5 2-7L2 9h7z"/></svg> ${esc(r)}</div>`;
}
function toast(msg){let t=$('#dkToast');if(!t){t=document.createElement('div');t.id='dkToast';t.className='toast';document.body.appendChild(t)}t.textContent=msg;t.classList.add('on');clearTimeout(t._tm);t._tm=setTimeout(()=>t.classList.remove('on'),1800)}
function nativeCall(name,...args){try{if(window.NativeApp&&typeof NativeApp[name]==='function')NativeApp[name](...args)}catch(e){}}
function setNativePlayback(on){nativeCall('setFullscreen',!!on);nativeCall('keepAwake',!!on)}
function autoPerformanceMode(){const dm=navigator.deviceMemory||8,save=!!navigator.connection?.saveData,cores=navigator.hardwareConcurrency||8;return save||dm<=4||cores<=4}
function isPerformanceMode(){const m=Settings?.get?.().performanceMode||'auto';return m==='on'||(m==='auto'&&autoPerformanceMode())}
function performanceModeLabel(){const m=Settings.get().performanceMode||'auto';return m==='auto'?(autoPerformanceMode()?'Auto: Aktif':'Auto'):(m==='on'?'Aktif':'Mati')}
function cyclePerformanceMode(){const cur=Settings.get().performanceMode||'auto',next=cur==='auto'?'on':(cur==='on'?'off':'auto');Settings.set('performanceMode',next);applyPerformanceMode();toast('Mode performa: '+performanceModeLabel())}
function applyPerformanceMode(){document.documentElement.classList.toggle('perf-mode',isPerformanceMode())}

function pruneApiCache(max=35,maxAge=3*24*60*60*1000){try{const now=Date.now();let items=Object.keys(localStorage).filter(k=>k.startsWith('dk_api_')).map(k=>{let t=0;try{t=JSON.parse(localStorage.getItem(k)||'{}').t||0}catch(e){}return{k,t}});items.forEach(x=>{if(x.t&&now-x.t>maxAge)localStorage.removeItem(x.k)});items=Object.keys(localStorage).filter(k=>k.startsWith('dk_api_')).map(k=>{let t=0;try{t=JSON.parse(localStorage.getItem(k)||'{}').t||0}catch(e){}return{k,t}}).sort((a,b)=>a.t-b.t);while(items.length>max)localStorage.removeItem(items.shift().k)}catch(e){}}
function cacheKey(url){try{return 'dk_api_'+btoa(unescape(encodeURIComponent(url))).slice(0,120)}catch(e){return 'dk_api_'+String(url).replace(/\W/g,'').slice(0,120)}}
const API_TIMEOUT_MS=12000;
const apiHealth={};
function noteApiHealth(url,ok,err){
  try{
    const host=(String(url).match(/^https?:\/\/[^/]+/)||['unknown'])[0];
    const cur=apiHealth[host]||{ok:0,fail:0,lastError:'',lastAt:0};
    if(ok){cur.ok++;cur.lastError=''}else{cur.fail++;cur.lastError=String(err||'error')}
    cur.lastAt=Date.now();apiHealth[host]=cur;
  }catch(e){}
}
function apiHealthSummary(){
  return Object.keys(apiHealth).map(h=>{const c=apiHealth[h];return {host:h,ok:c.ok,fail:c.fail,lastError:c.lastError,lastAt:c.lastAt}});
}
async function fetchWithTimeout(url,opts={},timeout=API_TIMEOUT_MS){
  const ctrl=typeof AbortController!=='undefined'?new AbortController():null;
  const timer=ctrl?setTimeout(()=>ctrl.abort(),timeout):null;
  try{
    const r=await fetch(url,{...opts,signal:ctrl?ctrl.signal:undefined});
    return r;
  }finally{if(timer)clearTimeout(timer)}
}
async function fetchJsonTimeout(url,timeout=API_TIMEOUT_MS){
  const r=await fetchWithTimeout(url,{cache:'no-store'},timeout);
  if(!r.ok)throw new Error('HTTP '+r.status);
  return r.json();
}
async function cachedJson(url,ttl=180000){
  const now=Date.now(),mem=jsonMemCache[url];if(mem&&now-mem.t<ttl)return mem.v;
  const k=cacheKey(url);let stale=null;
  try{const raw=localStorage.getItem(k);if(raw){const c=JSON.parse(raw);stale=c;if(now-c.t<ttl){jsonMemCache[url]=c;return c.v}}}catch(e){}
  try{
    const r=await fetchWithTimeout(url,{cache:'no-store'},API_TIMEOUT_MS);
    if(!r.ok)throw new Error('HTTP '+r.status);
    const v=await r.json();
    jsonMemCache[url]={t:now,v};
    noteApiHealth(url,true);
    try{localStorage.setItem(k,JSON.stringify({t:now,v}))}catch(e){pruneApiCache()}
    return v;
  }catch(e){
    const msg=e?.name==='AbortError'?'Timeout '+API_TIMEOUT_MS+'ms':String(e?.message||e);
    noteApiHealth(url,false,msg);
    ErrorLog.capture('api',url,{error:msg,offline:!navigator.onLine});
    if(stale?.v){
      if(!window.__dkStaleToastAt||Date.now()-window.__dkStaleToastAt>8000){
        window.__dkStaleToastAt=Date.now();
        toast(navigator.onLine?'Memakai cache (API lambat/gagal)':'Memakai cache offline');
      }
      return stale.v;
    }
    const err=new Error(msg);err.code=e?.name==='AbortError'?'TIMEOUT':'NETWORK';throw err;
  }
}
function rememberItems(items){(items||[]).forEach(d=>{if(!d||!d.drama_id)return;itemCache[d.drama_id]=d;if(!allItems.some(x=>x.drama_id===d.drama_id&&(x._p||platCache[x.drama_id])===(d._p||platCache[d.drama_id])))allItems.push(d)});if(allItems.length>700)allItems=allItems.slice(-700)}
function handleNativeBack(){if($('#epBd')?.classList.contains('on')){closeEpModal();return true}if($('#plOv')?.classList.contains('on')){closePl();return true}if($('#detOv')?.classList.contains('on')){closeDet();return true}if($('#sOv')?.classList.contains('on')){closeSearch();return true}if(curTab!=='home'){go('home');return true}return false}
window.handleNativeBack=handleNativeBack;
function getRemoteConfigUrl(){try{return localStorage.getItem('dk_remote_config_url')||REMOTE_CONFIG_URL}catch(e){return REMOTE_CONFIG_URL}}
function versionLess(a,b){const pa=String(a||'0').split('.').map(n=>parseInt(n)||0),pb=String(b||'0').split('.').map(n=>parseInt(n)||0);for(let i=0;i<Math.max(pa.length,pb.length);i++){const x=pa[i]||0,y=pb[i]||0;if(x<y)return true;if(x>y)return false}return false}
function platformState(p){return remoteConfig?.platforms?.[p]||{}}
function platformEnabled(p){return platformState(p).enabled!==false}
function platformReason(p){return platformState(p).reason||'Platform sedang maintenance'}
function featureEnabled(name){return remoteConfig?.features?.[name]!==false}
function applyRemoteConfig(cfg,source='remote'){
  if(!cfg||typeof cfg!=='object')return;
  remoteConfig=cfg;remoteConfigMeta={source,updated:Date.now(),url:getRemoteConfigUrl()};
  if(cfg.api&&typeof cfg.api==='object'){Object.keys(cfg.api).forEach(k=>{if(API[k]&&cfg.api[k])API[k]=cfg.api[k]})}
  updatePlatformAvailability();
  if(cfg.minAppVersion&&versionLess(APP_VERSION,cfg.minAppVersion)){setTimeout(()=>askConfirm('Update Dramaku tersedia',`Versi minimal yang disarankan ${cfg.minAppVersion}. Versi kamu ${APP_VERSION}. Update APK supaya fitur tetap stabil.`,'Mengerti'),800)}
  setTimeout(()=>showUpdatePrompt(false),1100)
}
async function loadRemoteConfig(force=false){
  const url=getRemoteConfigUrl(),cacheKey='dk_remote_config_cache';
  if(!force){try{const c=JSON.parse(localStorage.getItem(cacheKey)||'null');if(c?.cfg&&Date.now()-(c.t||0)<10*60*1000){applyRemoteConfig(c.cfg,'cache');return c.cfg}}catch(e){}}
  try{
    const r=await fetchWithTimeout(url+(url.includes('?')?'&':'?')+'t='+Date.now(),{cache:'no-store'},10000);
    if(!r.ok)throw new Error('HTTP '+r.status);
    const cfg=await r.json();
    localStorage.setItem(cacheKey,JSON.stringify({t:Date.now(),cfg}));
    applyRemoteConfig(cfg,'remote');
    return cfg;
  }catch(e){
    ErrorLog.capture('remote_config','Gagal load remote config',{url,error:String(e?.message||e)});
    try{const c=JSON.parse(localStorage.getItem(cacheKey)||'null');if(c?.cfg){applyRemoteConfig(c.cfg,'stale-cache');toast('Remote config memakai cache');return c.cfg}}catch(_){}
    // APK offline fallback: bundled remote-config.json next to index.html
    try{
      const localUrl=(location.protocol==='file:'?'./remote-config.json':'/remote-config.json');
      const lr=await fetchWithTimeout(localUrl,{cache:'no-store'},4000);
      if(lr.ok){const cfg=await lr.json();applyRemoteConfig(cfg,'bundled');return cfg}
    }catch(_){}
    return null;
  }
}
async function reloadRemoteConfig(){await loadRemoteConfig(true);renderSettings();toast(remoteConfig?'Remote config diperbarui':'Remote config gagal dimuat')}
function updatePlatformAvailability(){if(!remoteConfig)return;$$('.plat-opt').forEach(e=>{const p=e.dataset.p,off=!platformEnabled(p);e.classList.toggle('disabled',off);if(off)e.title=platformReason(p);else e.removeAttribute('title')})}
function firstEnabledPlatform(){return Object.keys(API).find(platformEnabled)||'melolo'}
function remoteMessageHtml(){const m=remoteConfig?.message;if(!m||m.enabled===false)return'';return`<div class="remote-banner ${m.type==='warning'?'warn':''}"><div class="remote-ico">${m.type==='warning'?'⚠️':'✨'}</div><div class="remote-copy"><b>${esc(m.title||'Info Dramaku')}</b><span>${esc(m.text||'')}</span></div></div>`}
function remoteConfigSettingsHtml(icon){const cfg=remoteConfig,src=remoteConfigMeta.source||'default',url=getRemoteConfigUrl();const status=cfg?'Aktif':'Default';const ver=cfg?.version?`v${cfg.version}`:'-';return`<section class="settings-sec"><h3 class="settings-sec-title">${icon} Remote Config</h3><div class="settings-card">${settingRow(icon,'Refresh remote config','Ambil endpoint, status platform, dan pengumuman terbaru',status,'reloadRemoteConfig()')}${settingRow(icon,'Sumber config',url,ver,'toast(\'Remote config URL tersimpan di app\')')}<div class="remote-config-note">Status: ${esc(src)} · ${cfg?.updatedAt?`Update: ${esc(cfg.updatedAt)}`:'Pakai endpoint bawaan jika config belum tersedia.'}</div></div></section>`}
function openExternalUrl(url){
  if(!url)return;
  let u=String(url).trim();
  if(!/^https?:\/\//i.test(u)&&!/^mailto:/i.test(u)){
    if(u.startsWith('t.me/')||u.startsWith('wa.me/'))u='https://'+u;
    else if(/^[\w.-]+\.[a-z]{2,}/i.test(u))u='https://'+u;
  }
  try{if(window.NativeApp?.openUrl){NativeApp.openUrl(u);return}}catch(e){}
  try{window.open(u,'_blank')}catch(e){location.href=u}
}
function latestUpdateInfo(){const u=remoteConfig?.update||{};return{latest:u.latestVersion||remoteConfig?.latestVersion||'',url:u.downloadUrl||remoteConfig?.downloadUrl||'https://github.com/SanzzAza/dramaku/releases/latest',changelog:u.changelog||remoteConfig?.changelog||[],force:!!u.force}}
function showUpdatePrompt(force=false){const u=latestUpdateInfo();if(!u.latest||!versionLess(APP_VERSION,u.latest))return false;if(!force&&localStorage.getItem('dk_skip_update')===u.latest&&!u.force)return false;const changes=Array.isArray(u.changelog)&&u.changelog.length?'\n\nYang baru:\n- '+u.changelog.slice(0,6).join('\n- '):'';askConfirm('Update Dramaku tersedia',`Versi ${u.latest} siap diunduh. Versi kamu ${APP_VERSION}.${changes}`,'Download',false).then(ok=>{if(ok)openExternalUrl(u.url);else localStorage.setItem('dk_skip_update',u.latest)});return true}
function showInfo(title,msg){return new Promise(resolve=>{const old=$('#dkConfirm');if(old)old.remove();const ov=document.createElement('div');ov.className='dk-confirm';ov.id='dkConfirm';ov.innerHTML=`<div class="dk-confirm-card" role="dialog" aria-modal="true"><div class="dk-confirm-body"><div class="dk-confirm-title">${esc(title)}</div><div class="dk-confirm-msg" style="white-space:pre-line">${esc(msg)}</div></div><div class="dk-confirm-actions"><button class="dk-confirm-ok" style="flex:1" type="button">Mengerti</button></div></div>`;document.body.appendChild(ov);const done=()=>{ov.remove();resolve(true)};ov.querySelector('.dk-confirm-ok').onclick=done;ov.addEventListener('click',e=>{if(e.target===ov)done()})})}
function showAbout(){showInfo('Tentang Dramaku',`Dramaku adalah aplikasi agregator mini drama & film dari beberapa platform.\n\nDramaku tidak meng-host / menyimpan video di server sendiri. Semua konten dan hak cipta milik platform atau pemilik masing-masing.\n\nGunakan aplikasi ini secara pribadi dan hormati ketentuan platform sumber.\n\nVersi: ${APP_VERSION}`)}
function showPrivacy(){showInfo('Privasi & Data',`Dramaku menyimpan data hanya di perangkat kamu (localStorage), misalnya:\n• riwayat & favorit\n• progress tontonan\n• pengaturan aplikasi\n• cache API ringan\n\nData ini tidak dikirim ke server Dramaku. Clear data/cache aplikasi akan menghapusnya.\n\nLaporan episode (jika dipakai) dikirim lewat kontak support yang kamu pilih (WhatsApp/Telegram/email) dan berisi metadata episode bermasalah, bukan isi video.\n\nRemote config diambil dari repositori publik untuk status platform & update.`)}
function showDisclaimer(){showInfo('Disclaimer',`Dramaku hanya menyediakan antarmuka untuk menelusuri & memutar tautan yang tersedia dari platform sumber.\n\n• Bukan layanan resmi platform manapun\n• Tidak mengklaim kepemilikan konten\n• Ketersediaan / kualitas video tergantung platform sumber\n• Jika kamu pemilik hak cipta dan ingin menonaktifkan sumber tertentu, hubungi support lewat menu laporan\n\nDengan memakai Dramaku, kamu memahami batasan di atas.`)}
function reportTargetUrl(text){const s=remoteConfig?.support||{};if(s.whatsapp)return`https://wa.me/${String(s.whatsapp).replace(/\D/g,'')}?text=${encodeURIComponent(text)}`;if(s.telegram)return`https://t.me/share/url?url=${encodeURIComponent(location.href)}&text=${encodeURIComponent(text+'\n')}`;if(s.email)return`mailto:${encodeURIComponent(s.email)}?subject=${encodeURIComponent('Laporan Episode Dramaku')}&body=${encodeURIComponent(text)}`;return''}


const ErrorLog={
  list(){try{return JSON.parse(localStorage.getItem('dk_errors')||'[]')}catch(e){return[]}},
  capture(type,message,detail){try{let logs=this.list();logs.unshift({type,message:String(message||''),detail:detail||'',time:Date.now(),ua:navigator.userAgent});localStorage.setItem('dk_errors',JSON.stringify(logs.slice(0,80)))}catch(e){}},
  clear(){try{localStorage.removeItem('dk_errors')}catch(e){}},
  text(){return this.list().map(x=>`[${new Date(x.time).toLocaleString()}] ${x.type}: ${x.message}\n${typeof x.detail==='string'?x.detail:JSON.stringify(x.detail,null,2)}`).join('\n\n')}
};
window.addEventListener('error',e=>ErrorLog.capture('js',e.message,{file:e.filename,line:e.lineno,col:e.colno}));
window.addEventListener('unhandledrejection',e=>ErrorLog.capture('promise',e.reason?.message||e.reason||'Unhandled rejection',String(e.reason?.stack||e.reason||'')));
const Settings={
  defaults:{haptic:true,dataSaver:false,autoNext:true,nativeShare:true,performanceMode:'auto'},
  get(){try{return {...this.defaults,...JSON.parse(localStorage.getItem('dk_settings')||'{}')}}catch(e){return {...this.defaults}}},
  set(k,v){const s=this.get();s[k]=v;try{localStorage.setItem('dk_settings',JSON.stringify(s))}catch(e){};applyPerformanceMode();renderSettings()}
};
function haptic(type='light'){try{if(Settings.get().haptic&&window.NativeApp?.haptic)NativeApp.haptic(type)}catch(e){}}
function bumpEl(el){if(!el||isPerformanceMode())return;el.classList.remove('bump');void el.offsetWidth;el.classList.add('bump');setTimeout(()=>el.classList.remove('bump'),280)}
function watchMetaFor(id){
  const hid=String(id||'');
  if(!hid)return null;
  try{
    const h=(typeof getHistory==='function'?getHistory():[]).find(x=>String(x.id)===hid);
    if(h)return {ep:parseInt(h.ep)||1,pct:Math.max(0,Math.min(100,parseInt(h.pct||0)||0)),plat:h.plat||'',time:h.time||0};
  }catch(e){}
  return null;
}
function isFreshItem(d){
  // heuristic: free flag / "new" fields / recent tags
  if(!d)return false;
  if(d.is_new_book==='1'||d.is_new===true||d.isNew===true)return true;
  const tags=(Array.isArray(d.tags)?d.tags:[]).map(t=>String(typeof t==='object'?(t.name||t.title||''):t).toLowerCase());
  if(tags.some(t=>/\b(new|baru|latest|hot|trending)\b/.test(t)))return true;
  const name=String(d.drama_name||d.title||'').toLowerCase();
  return /\b(new|baru)\b/.test(name);
}
function progressRingHtml(pct,size=34){
  pct=Math.max(0,Math.min(100,parseInt(pct)||0));
  if(!pct)return '';
  const r=13,c=2*Math.PI*r,off=c*(1-pct/100);
  return `<div class="prog-ring" style="width:${size}px;height:${size}px" aria-label="Progress ${pct}%"><svg viewBox="0 0 32 32"><circle class="pr-bg" cx="16" cy="16" r="${r}"/><circle class="pr-fg" cx="16" cy="16" r="${r}" style="stroke-dasharray:${c.toFixed(2)};stroke-dashoffset:${off.toFixed(2)}"/></svg><span>${pct}%</span></div>`;
}

function appVersion(){try{return window.NativeApp?.getVersion?.()||APP_VERSION}catch(e){return APP_VERSION}}
function clearApiCache(){try{Object.keys(localStorage).filter(k=>k.startsWith('dk_api_')).forEach(k=>localStorage.removeItem(k));Object.keys(jsonMemCache).forEach(k=>delete jsonMemCache[k]);toast('Cache API dibersihkan')}catch(e){toast('Gagal membersihkan cache')}}
function clearWebViewCache(){clearApiCache();nativeCall('clearWebViewCache');toast('Cache WebView dibersihkan')}
function askConfirm(title,msg,okText='Oke',danger=false){return new Promise(resolve=>{const old=$('#dkConfirm');if(old)old.remove();const ov=document.createElement('div');ov.className='dk-confirm';ov.id='dkConfirm';ov.innerHTML=`<div class="dk-confirm-card" role="dialog" aria-modal="true"><div class="dk-confirm-body"><div class="dk-confirm-title">${esc(title)}</div><div class="dk-confirm-msg">${esc(msg)}</div></div><div class="dk-confirm-actions"><button class="dk-confirm-cancel" type="button">Batal</button><button class="dk-confirm-ok ${danger?'danger':''}" type="button">${esc(okText)}</button></div></div>`;document.body.appendChild(ov);const done=v=>{ov.remove();resolve(v)};ov.querySelector('.dk-confirm-cancel').onclick=()=>done(false);ov.querySelector('.dk-confirm-ok').onclick=()=>done(true);ov.addEventListener('click',e=>{if(e.target===ov)done(false)});})}
function clearAllHistory(){askConfirm('Hapus riwayat tontonan?','Semua riwayat dan progress yang tersimpan akan dihapus dari perangkat ini.','Hapus',true).then(ok=>{if(ok){localStorage.removeItem('dk_history');renderSettings();renderHistory();toast('Riwayat dihapus')}})}
function clearAllFavs(){askConfirm('Hapus semua favorit?','Daftar drama favorit akan dikosongkan dari perangkat ini.','Hapus',true).then(ok=>{if(ok){localStorage.removeItem('dk_favs');renderSettings();renderFav();toast('Favorit dihapus')}})}
function copyErrorLogs(){const txt=ErrorLog.text()||'Tidak ada log error';navigator.clipboard?.writeText(txt);toast('Log error disalin')}
function clearErrorLogs(){ErrorLog.clear();renderSettings();toast('Log error dihapus')}
function setFitMode(mode){fitMode=mode;try{localStorage.setItem('dk_fit_mode',fitMode)}catch(e){}applyFitMode();renderSettings()}
function nativeShare(title,text,url){try{if(Settings.get().nativeShare&&window.NativeApp?.share){NativeApp.share(title||'Dramaku',text||'',url||location.href);return true}}catch(e){}return false}
function settingIcon(path){return `<svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="${path}"/></svg>`}
function settingRow(icon,title,sub,value,onclick,danger=false){return `<button class="setting-row" onclick="${onclick}"><span class="setting-ico">${icon}</span><span class="setting-copy"><b>${esc(title)}</b><span>${esc(sub)}</span></span>${value?`<span class="setting-value ${danger?'danger-value':''}">${esc(value)}</span>`:''}</button>`}
function settingSwitch(icon,title,sub,on,onclick){return `<button class="setting-row" onclick="${onclick}"><span class="setting-ico">${icon}</span><span class="setting-copy"><b>${esc(title)}</b><span>${esc(sub)}</span></span><span class="setting-switch ${on?'on':''}"></span></button>`}
function renderSettings(){const box=$('#v-settings');if(!box)return;const s=Settings.get(),errs=ErrorLog.list(),h=getHistory(),f=getFavs(),apiCount=Object.keys(localStorage).filter(k=>k.startsWith('dk_api_')).length;const iGear=settingIcon('M19.43 12.98c.04-.32.07-.65.07-.98s-.02-.66-.07-.98l2.11-1.65-2-3.46-2.49 1a7.28 7.28 0 00-1.69-.98L14.5 2h-5l-.38 2.93c-.6.23-1.16.55-1.69.98l-2.49-1-2 3.46 2.11 1.65c-.04.32-.08.65-.08.98s.03.66.08.98l-2.11 1.65 2 3.46 2.49-1c.53.4 1.09.73 1.69.98L9.5 22h5l.38-2.93c.6-.25 1.16-.58 1.69-.98l2.49 1 2-3.46-2.11-1.65zM12 15.5A3.5 3.5 0 1112 8a3.5 3.5 0 010 7.5z');const iPlay=settingIcon('M8 5v14l11-7z');const iTrash=settingIcon('M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 4l1-1h6l1 1h4v2H4V4h4z');const iBug=settingIcon('M20 8h-2.81a5.985 5.985 0 00-1.82-1.96L16 4.5 14.5 3l-1.17 2.33A6.58 6.58 0 0012 5c-.46 0-.91.05-1.33.14L9.5 3 8 4.5l.63 1.54A5.985 5.985 0 006.81 8H4v2h2.09c-.05.33-.09.66-.09 1v1H4v2h2v1c0 .34.04.67.09 1H4v2h2.81A6.011 6.011 0 0012 21a6.011 6.011 0 005.19-3H20v-2h-2.09c.05-.33.09-.66.09-1v-1h2v-2h-2v-1c0-.34-.04-.67-.09-1H20V8z');const logs=errs.length?errs.slice(0,8).map(x=>`<div class="error-log"><b>${esc(x.type)} · ${esc(x.message)}</b><div class="error-time">${new Date(x.time).toLocaleString()}</div><code>${esc(typeof x.detail==='string'?x.detail:JSON.stringify(x.detail,null,2))}</code></div>`).join(''):'<div class="empty-state" style="padding:22px"><p>Tidak ada error tersimpan</p><p class="empty-sub">Kalau ada crash/API error, log akan muncul di sini.</p></div>';box.innerHTML=`<div class="settings-page"><div class="settings-hero"><div class="settings-kicker">Dramaku Control Center</div><div class="settings-title">Setelan & Diagnostik</div><div class="settings-sub">Atur pengalaman nonton, bersihkan cache, dan cek log error untuk build APK yang lebih matang.</div><div class="app-version">Versi aplikasi: ${esc(appVersion())}</div></div><div class="settings-grid"><section class="settings-sec"><h3 class="settings-sec-title">${iGear} Preferensi</h3><div class="settings-card">${settingRow(iPlay,'Mode Video','Full memenuhi layar, Asli menampilkan rasio original',fitMode==='contain'?'Asli':'Full',`setFitMode('${fitMode==='contain'?'cover':'contain'}');toast('Mode video diubah')`)}${settingRow(iGear,'Mode Performa','Auto untuk HP low-end: kurangi blur, animasi, shadow, dan jumlah card',performanceModeLabel(),'cyclePerformanceMode()')}${settingSwitch(iGear,'Haptic feedback','Getar halus saat tap tombol di APK',s.haptic,`Settings.set('haptic',${!s.haptic})`)}${settingSwitch(iPlay,'Auto next episode','Episode berikutnya otomatis saat video selesai',s.autoNext,`Settings.set('autoNext',${!s.autoNext})`)}${settingSwitch(iGear,'Mode hemat data','Prioritaskan kualitas lebih ringan saat memungkinkan',s.dataSaver,`Settings.set('dataSaver',${!s.dataSaver})`)}${settingSwitch(iGear,'Native share','Pakai Android share sheet jika tersedia',s.nativeShare,`Settings.set('nativeShare',${!s.nativeShare})`)}${settingRow(iGear,'Tampilkan onboarding','Buka ulang panduan awal pengguna','Buka',`localStorage.removeItem('dk_onboard_done');showOnboarding(true)`)}${settingRow(iGear,'Cek update','Periksa versi APK terbaru dari remote config','Cek','showUpdatePrompt(true)')}${settingRow(iGear,'Tentang aplikasi','Informasi Dramaku dan versi','Buka','showAbout()')}${settingRow(iGear,'Privasi & Data','Data lokal di perangkat, cache, laporan','Buka','showPrivacy()')}${settingRow(iGear,'Disclaimer','Hak cipta, agregator, batasan layanan','Buka','showDisclaimer()')}</div></section><section class="settings-sec"><h3 class="settings-sec-title">${iTrash} Penyimpanan</h3><div class="settings-card">${settingRow(iTrash,'Bersihkan cache API',`${apiCount} cache tersimpan`,apiCount+' item','clearApiCache()')}${settingRow(iTrash,'Bersihkan cache WebView','Membersihkan cache native Android WebView','APK','clearWebViewCache()')}${settingRow(iTrash,'Hapus riwayat',`${h.length} drama tersimpan`,h.length+' item','clearAllHistory()',true)}${settingRow(iTrash,'Hapus favorit',`${f.length} drama favorit`,f.length+' item','clearAllFavs()',true)}</div></section></div>${remoteConfigSettingsHtml(iGear)}<section class="settings-sec"><h3 class="settings-sec-title">${iBug} Error Reporting</h3><div class="settings-card">${settingRow(iBug,'Salin log error','Kirim log ini kalau build/API/player bermasalah',errs.length+' log','copyErrorLogs()')}${settingRow(iTrash,'Hapus log error','Kosongkan semua catatan error lokal','Reset','clearErrorLogs()',true)}${logs}</div></section></div>`}


function brandSvg(size=46){return `<svg class="brand-mark" width="${size}" height="${size}" viewBox="0 0 64 64" fill="none" aria-hidden="true"><path d="M17 12h13.5C42.9 12 51 20 51 32s-8.1 20-20.5 20H17V12Z" fill="rgba(255,255,255,.96)"/><path d="M27 23v18.5l14.5-9.25L27 23Z" class="play" fill="#10f5a6"/><path d="M47.5 8l2.4 5.7 5.9 2.3-5.9 2.4-2.4 5.8-2.4-5.8-5.8-2.4 5.8-2.3L47.5 8Z" class="spark" fill="#effff7"/><path d="M54 25l1.1 2.6 2.7 1.1-2.7 1.1L54 32.4l-1.1-2.6-2.6-1.1 2.6-1.1L54 25Z" fill="#34d399"/></svg>`}
function showOnboarding(force=false){if(!force&&localStorage.getItem('dk_onboard_done'))return;if($('#onboard'))return;const s=Settings.get();const el=document.createElement('div');el.className='onboard';el.id='onboard';el.innerHTML=`<div class="onboard-card"><div class="onboard-logo">${brandSvg(46)}</div><div class="onboard-title">Selamat datang di Dramaku</div><div class="onboard-sub">Atur pengalaman nonton pertama kamu. Bisa diubah lagi kapan saja lewat Setelan.</div><div class="onboard-choices"><button class="onb-choice fit ${fitMode==='cover'?'on':''}" onclick="setFitMode('cover');document.querySelectorAll('.onb-choice.fit').forEach(x=>x.classList.remove('on'));this.classList.add('on')"><span class="onb-ico">▣</span><span class="onb-copy"><span class="onb-choice-title">Video Full Layar</span><span class="onb-choice-sub">Mengisi layar HP, cocok untuk mini drama vertikal.</span></span></button><button class="onb-choice fit ${fitMode==='contain'?'on':''}" onclick="setFitMode('contain');document.querySelectorAll('.onb-choice.fit').forEach(x=>x.classList.remove('on'));this.classList.add('on')"><span class="onb-ico">□</span><span class="onb-copy"><span class="onb-choice-title">Video Ukuran Asli</span><span class="onb-choice-sub">Video tidak terpotong, tapi bisa ada bar hitam.</span></span></button><button class="onb-choice ${s.dataSaver?'on':''}" onclick="onboardToggleData(this)"><span class="onb-ico">↯</span><span class="onb-copy"><span class="onb-choice-title">Mode Hemat Data</span><span class="onb-choice-sub">${s.dataSaver?'Aktif':'Nonaktif'} — prioritaskan stream lebih ringan jika tersedia.</span></span></button></div><button class="onboard-start" onclick="finishOnboarding()">Mulai nonton</button><button class="onboard-skip" onclick="finishOnboarding()">Lewati dulu</button></div>`;document.body.appendChild(el)}
function onboardToggleData(btn){const next=!Settings.get().dataSaver;Settings.set('dataSaver',next);btn.classList.toggle('on',next);const sub=btn.querySelector('.onb-choice-sub');if(sub)sub.textContent=(next?'Aktif':'Nonaktif')+' — prioritaskan stream lebih ringan jika tersedia.'}
function finishOnboarding(){try{localStorage.setItem('dk_onboard_done','1')}catch(e){};$('#onboard')?.remove();toast('Dramaku siap dipakai')}
function reportEpisode(btn){if(!featureEnabled('reportEpisode')){toast('Fitur laporan sedang dinonaktifkan');return}const data={platform:curDrama?._p||P,id:curDrama?.drama_id||'',title:curDrama?.drama_name||'',episode:curPE,time:new Date().toISOString(),fitMode,app:APP_VERSION};ErrorLog.capture('video_report','Laporan episode bermasalah',data);const text=`Laporan Episode Bermasalah - Dramaku

Judul: ${data.title}
Platform: ${platformLabel(data.platform)}
Drama ID: ${data.id}
Episode: ${data.episode}
Versi APK: ${data.app}
Waktu: ${data.time}`;const target=reportTargetUrl(text);if(target){openExternalUrl(target);toast('Membuka kontak laporan...')}else if(!nativeShare('Laporan Episode Dramaku',text,location.href)){navigator.clipboard?.writeText(text);toast('Laporan disalin ke clipboard')}else toast('Laporan siap dibagikan');if(btn)btn.classList.add('reported')}

document.addEventListener('click',e=>{if(e.target.closest('button,.card,.tab,.plat-opt,.mood-card,.spotlight-card'))haptic('light')},{capture:true});


let descOn=0;
let uiTimer=null;
