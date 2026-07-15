/* Dramaku bootstrap */
function updateConnBanner(){
  let b=$('#dkConnBanner');
  if(!b){
    b=document.createElement('div');
    b.id='dkConnBanner';
    b.className='conn-banner';
    b.setAttribute('role','status');
    document.body.appendChild(b);
  }
  if(!navigator.onLine){
    b.textContent='Offline — menampilkan cache jika tersedia';
    b.classList.add('on','offline');
  }else{
    b.classList.remove('offline');
    if(b.dataset.soft==='1'){
      b.textContent='Online kembali';
      b.classList.add('on');
      clearTimeout(b._tm);
      b._tm=setTimeout(()=>{b.classList.remove('on');b.dataset.soft='0'},2200);
    }else b.classList.remove('on');
  }
}
async function softHealthCheck(){
  try{
    const base=API[firstEnabledPlatform()]||Object.values(API)[0];
    if(!base)return;
    const r=await fetchWithTimeout(base+(base.includes('?')?'&':'?')+'health=1',{method:'GET',cache:'no-store'},5000);
    noteApiHealth(base,!!r&&r.status<500,r?('HTTP '+r.status):'no-response');
  }catch(e){noteApiHealth(Object.values(API)[0]||'api',false,e?.message||e)}
}

window.onNativePlayerResult=function(dramaId,episode,platform,posSec,durSec,ended){
  try{
    const id=String(dramaId||'');
    const ep=Math.max(1,Number(episode)||1);
    const pos=Math.max(0,Number(posSec)||0);
    const dur=Math.max(0,Number(durSec)||0);
    if(id && dur>0){
      if(ended || (dur>0 && pos>=dur-3)){
        clearWatchProgress(id,ep);
      }else if(pos>2){
        saveWatchProgress(id,ep,pos,dur);
      }
    }
    // Update history entry if this drama is current or known
    let drama=curDrama && String(curDrama.drama_id)===id ? curDrama : (itemCache[id]||null);
    if(!drama && id){
      drama={drama_id:id,drama_name:$('#plName')?.textContent||'Drama',_p:platform||platCache[id]||P,_thumb:thumbCache[id]||''};
    }
    if(drama){
      if(platform) drama._p=platform;
      saveHistory(drama,ep);
    }
    try{if(curTab==='history')renderHistory(); if(curTab==='home'||curTab==='profile'){/* soft */}}catch(e){}
    if(ended){
      toast('Episode '+ep+' selesai');
      // Optional auto-next only if same drama still open in web player context
      if(Settings.get().autoNext && curDrama && String(curDrama.drama_id)===id){
        const total=parseInt(curDrama.episode_count||curEps.length||ep)||ep;
        if(ep<total){
          setTimeout(()=>play(id,ep+1).catch(()=>{}),400);
        }
      }
    }else if(pos>5){
      toast('Progress disimpan · Ep '+ep);
    }
  }catch(e){ErrorLog.capture('native_player_result',String(e?.message||e));}
};

async function boot(){
  try{
    applyPerformanceMode();pruneApiCache();updateConnBanner();
    try{await loadRemoteConfig()}catch(e){ErrorLog.capture('boot_config',String(e?.message||e))}
    const savedPlatform=(()=>{try{return localStorage.getItem('dk_platform')}catch(e){return null}})();
    const startP=(savedPlatform&&API[savedPlatform]&&platformEnabled(savedPlatform))?savedPlatform:firstEnabledPlatform();
    try{
      if(startP!==P)setPlatform(startP);else await loadTab('home');
    }catch(e){
      ErrorLog.capture('boot_home',String(e?.message||e));
      const box=$('#v-home');if(box)box.innerHTML=errHtml(e?.message||e);
    }
    updatePlatformAvailability();
    setTimeout(()=>{try{showOnboarding()}catch(e){}},1200);
    setTimeout(()=>{try{softHealthCheck()}catch(e){}},1800);
  }catch(e){
    ErrorLog.capture('boot_fatal',String(e?.message||e));
    try{toast('Gagal memulai app, coba muat ulang')}catch(_){}
  }
}
window.addEventListener('online',()=>{const b=$('#dkConnBanner');if(b)b.dataset.soft='1';updateConnBanner();toast('Koneksi kembali online')});
window.addEventListener('offline',()=>{updateConnBanner();toast('Kamu sedang offline')});
boot().catch(e=>{try{ErrorLog.capture("boot_promise",String(e?.message||e));toast("Gagal memulai Dramaku")}catch(_){}});
