/* Dramaku history, favorites, profile, clips */
function debounce(fn,ms){let t;return function(...a){clearTimeout(t);t=setTimeout(()=>fn.apply(this,a),ms)}}
/* ===== WATCH PROGRESS (localStorage) ===== */
function progressKey(id,ep){return 'dk_prog_'+id+'_'+ep}
function getWatchProgress(id,ep){try{return JSON.parse(localStorage.getItem(progressKey(id,ep))||'null')}catch(e){return null}}
function clearWatchProgress(id,ep){try{localStorage.removeItem(progressKey(id,ep))}catch(e){}}
function fmtTime(sec){sec=Math.max(0,Math.floor(+sec||0));const m=Math.floor(sec/60),s=sec%60;return m+':'+String(s).padStart(2,'0')}
function saveWatchProgress(id,ep,pos,dur){
  if(!id||!ep||!dur||!isFinite(dur))return;
  const pct=Math.max(0,Math.min(100,Math.round((pos/dur)*100)));
  const data={pos:Math.floor(pos),dur:Math.floor(dur),pct,updated:Date.now()};
  try{localStorage.setItem(progressKey(id,ep),JSON.stringify(data))}catch(e){}
  try{let h=getHistory();const i=h.findIndex(x=>x.id===id);if(i>=0&&Number(h[i].ep)===Number(ep)){h[i]={...h[i],pos:data.pos,dur:data.dur,pct:data.pct,time:Date.now()};localStorage.setItem('dk_history',JSON.stringify(h))}}catch(e){}
}

/* ===== HISTORY (localStorage) ===== */
function getHistory(){try{return JSON.parse(localStorage.getItem('dk_history')||'[]')}catch(e){return[]}}
function saveHistory(drama,ep){
  let h=getHistory();h=h.filter(x=>x.id!==drama.drama_id);
  const pr=getWatchProgress(drama.drama_id,ep)||{};
  h.unshift({id:drama.drama_id,name:drama.drama_name,thumb:drama._thumb||thumbCache[drama.drama_id]||'',ep:ep,plat:drama._p||P,time:Date.now(),pos:pr.pos||0,dur:pr.dur||0,pct:pr.pct||0});
  if(h.length>50)h=h.slice(0,50);
  localStorage.setItem('dk_history',JSON.stringify(h));
}
function renderHistory(){
  const box=$('#v-history'),h=getHistory();
  if(!h.length){box.innerHTML=emptyHtml('Belum ada riwayat tontonan','Drama yang kamu tonton akan muncul di sini');return}
  box.innerHTML=`<div class="sec"><div class="sec-hd"><h2 class="sec-tt">Riwayat Tontonan</h2><div class="sec-more" onclick="clearAllHistory()">Hapus</div></div><div class="grid">${h.map(d=>{
    const thumb=fixImg(d.thumb||'');platCache[d.id]=d.plat;thumbCache[d.id]=thumb;
    const pct=Math.max(0,Math.min(100,parseInt(d.pct||0)||0));
    const ep=parseInt(d.ep)||1;
    const prog=pct?`<div class="card-progress"><span style="width:${pct}%"></span></div>`:'';
    const ring=pct?progressRingHtml(pct,30):'';
    return`<article class="card" role="button" tabindex="0" onclick="openDet('${jsStr(d.id)}','${jsStr(thumb)}')"><div class="card-img"><img src="${esc(thumb)}" alt="${esc(d.name)}" loading="lazy" decoding="async" onerror="this.style.display='none'"/><div class="badge-ep">Ep ${ep}</div><div class="badge-cont">LANJUT${pct?` ${pct}%`:''}</div><div class="badge-plat">${esc(platformLabel(d.plat))}</div>${prog}${ring}</div><div class="card-body"><div class="card-name">${esc(d.name)}</div><div class="card-sub">Episode ${ep}${pct?` · ${pct}% ditonton`:''}</div></div></article>`;
  }).join('')}</div></div>`;
}

/* ===== FAVORITES (localStorage) ===== */
function getFavs(){try{return JSON.parse(localStorage.getItem('dk_favs')||'[]')}catch(e){return[]}}
function toggleFav(drama){
  let f=getFavs();const idx=f.findIndex(x=>x.id===drama.drama_id);
  if(idx>=0){f.splice(idx,1)}else{f.unshift({id:drama.drama_id,name:drama.drama_name,thumb:drama._thumb||thumbCache[drama.drama_id]||'',plat:drama._p||P,ep:drama.episode_count||0})}
  localStorage.setItem('dk_favs',JSON.stringify(f));return idx<0;
}
function isFav(id){return getFavs().some(x=>x.id===id)}
function toggleFavBtn(btn){if(!curDrama)return;const on=toggleFav(curDrama);bumpEl(btn);haptic(on?'heavy':'light');btn.classList.toggle('on',on);btn.style.background=on?'var(--accent)':'var(--bg3)';toast(on?'Ditambahkan ke favorit':'Dihapus dari favorit')}
function renderFav(){
  const box=$('#v-fav'),f=getFavs();
  if(!f.length){box.innerHTML=emptyHtml('Belum ada drama favorit','Tap bookmark di halaman detail untuk menyimpan');return}
  box.innerHTML=`<div class="sec"><div class="sec-hd"><h2 class="sec-tt">Drama Favorit</h2></div><div class="grid">${f.map(d=>{
    const thumb=fixImg(d.thumb||'');platCache[d.id]=d.plat;thumbCache[d.id]=thumb;
    return`<article class="card" role="button" tabindex="0" onclick="openDet('${jsStr(d.id)}','${jsStr(thumb)}')"><div class="card-img"><img src="${esc(thumb)}" alt="${esc(d.name)}" loading="lazy" decoding="async" onerror="this.style.display='none'"/>${d.ep?`<div class="badge-ep">${parseInt(d.ep)||0} Ep</div>`:''}<div class="badge-plat">${esc(platformLabel(d.plat))}</div></div><div class="card-body"><div class="card-name">${esc(d.name)}</div></div></article>`;
  }).join('')}</div></div>`;
}
function profileMiniCard(d,type='history'){
  const thumb=fixImg(d.thumb||''),ep=parseInt(d.ep)||1,pct=Math.max(0,Math.min(100,parseInt(d.pct||0)||0));
  if(d.id){platCache[d.id]=d.plat;thumbCache[d.id]=thumb}
  return`<article class="profile-mini" onclick="openDet('${jsStr(d.id)}','${jsStr(thumb)}')"><div class="profile-mini-img"><img src="${esc(thumb)}" alt="${esc(d.name)}" loading="lazy" decoding="async" onerror="this.style.display='none'"/>${pct?`<div class="profile-mini-progress"><span style="width:${pct}%"></span></div>`:''}</div><div class="profile-mini-copy"><b>${esc(d.name||'Tanpa Judul')}</b><span>${type==='history'?`Episode ${ep}${pct?` · ${pct}%`:''}`:(d.ep?`${d.ep} Episode`:'Favorit')}</span></div></article>`
}
function renderProfile(){
  const box=$('#v-profile'),h=getHistory(),f=getFavs(),errs=ErrorLog.list();
  const last=h[0],apiCount=Object.keys(localStorage).filter(k=>k.startsWith('dk_api_')).length;
  const watched=h.length,fav=f.length,progress=h.filter(x=>x.pct>0).length;
  const lastBlock=last?`<div class="profile-resume" onclick="resumeWatch('${jsStr(last.id)}','${jsStr(last.thumb)}','${jsStr(last.plat)}',${parseInt(last.ep)||1})"><div class="resume-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg></div><div class="resume-copy"><div class="resume-kicker">Terakhir ditonton</div><div class="resume-title">${esc(last.name)}</div><div class="resume-sub">Lanjut Episode ${parseInt(last.ep)||1}${last.pct?` · ${last.pct}%`:''}</div>${last.pct?`<div class="resume-bar"><span style="width:${Math.max(0,Math.min(100,parseInt(last.pct)||0))}%"></span></div>`:''}</div></div>`:'';
  const historyRows=h.slice(0,5).map(d=>profileMiniCard(d,'history')).join('');
  const favRows=f.slice(0,5).map(d=>profileMiniCard(d,'fav')).join('');
  box.innerHTML=`<div class="profile-page"><section class="profile-hero"><div class="profile-avatar">${brandSvg(42)}</div><div class="profile-hero-copy"><div class="profile-kicker">Dramaku Profile</div><h1>Saya</h1><p>Kontrol tontonan, koleksi, cache, dan diagnostik aplikasi.</p></div></section><div class="profile-stats"><div><b>${watched}</b><span>Ditonton</span></div><div><b>${fav}</b><span>Favorit</span></div><div><b>${progress}</b><span>Progress</span></div><div><b>${errs.length}</b><span>Error</span></div></div>${lastBlock}<section class="profile-actions"><button onclick="go('fav')">🔖<span>Favorit</span></button><button onclick="go('history')">🕘<span>Riwayat</span></button><button onclick="go('settings')">⚙️<span>Setelan</span></button><button onclick="reloadRemoteConfig()">☁️<span>Config</span></button></section><div class="profile-grid"><section class="profile-section"><div class="profile-section-head"><h3>Lanjut nonton</h3><button onclick="go('history')">Semua</button></div>${historyRows||'<div class="profile-empty">Belum ada riwayat tontonan.</div>'}</section><section class="profile-section"><div class="profile-section-head"><h3>Koleksi favorit</h3><button onclick="go('fav')">Semua</button></div>${favRows||'<div class="profile-empty">Belum ada favorit.</div>'}</section></div><section class="profile-section profile-tools"><div class="profile-section-head"><h3>Tools cepat</h3></div><button onclick="clearApiCache()"><span>Bersihkan cache API</span><b>${apiCount} item</b></button><button onclick="copyErrorLogs()"><span>Salin log error</span><b>${errs.length} log</b></button><button onclick="localStorage.removeItem('dk_onboard_done');showOnboarding(true)"><span>Tampilkan onboarding</span><b>Buka</b></button><button onclick="showUpdatePrompt(true)"><span>Cek update APK</span><b>v${APP_VERSION}</b></button><button onclick="showAbout()"><span>Tentang & Disclaimer</span><b>Buka</b></button></section></div>`;
}

function clipFeedUrl(p,page=1){const base=API[p];if(p==='drakor')return base+`/trending?page=${page}&limit=30&days=30`;if(p==='dramabox')return base+'/rank?lang=in';if(p==='moviebox')return base+`/global?page=${page}&perPage=20`;if(p==='goodshort')return base+`/populer?page=${page}`;if(p==='reelshort')return base+`/populer?page=${page}&limit=20&period=0&rule=0`;if(p==='dramanova')return base+`/discovery?size=20&page=${page}`;if(p==='netshort')return base+'/populer';if(p==='flickreels')return base+'/populer';return base+`/populer?page=${page}&lang=id`}
function clipSkeleton(){return`<div class="clips-page"><div class="clips-hero skel"></div><div class="clip-chip-row"><div class="skel clip-chip-skel"></div><div class="skel clip-chip-skel"></div><div class="skel clip-chip-skel"></div></div><div class="clip-grid">${skelHtml(8,1)}</div></div>`}
function clipCardHtml(d,i){const img=fixImg(d.thumb_url||''),id=jsStr(d.drama_id),si=jsStr(img),pl=d._p||platCache[d.drama_id]||P,nm=d.drama_name||'Tanpa Judul',tags=(d.tags||[]).slice(0,2).map(t=>typeof t==='object'?(t.name||t.title||''):t).filter(Boolean).join(' • '),views=d.watch_value?fmtV(d.watch_value):'',ep=d.episode_count||'';return`<article class="clip-card" onclick="openDet('${id}','${si}')"><div class="clip-poster"><img src="${esc(img)}" alt="${esc(nm)}" loading="lazy" decoding="async" onerror="this.style.display='none'"/><div class="clip-rank">${i+1}</div>${views?`<div class="clip-views">🔥 ${esc(views)}</div>`:''}<button class="clip-play" onclick="event.stopPropagation();resumeWatch('${id}','${si}','${jsStr(pl)}',1,true)"><svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg><span>Ep1</span></button></div><div class="clip-copy"><b>${esc(nm)}</b><span>${esc(tags||platformLabel(pl))}${ep?` | ${esc(ep)} episode`:''}</span></div></article>`}
function clipMiniPlayerHtml(){const last=getHistory()[0];if(!last)return'';const thumb=fixImg(last.thumb||''),ep=parseInt(last.ep)||1;return`<div class="clip-mini-player" id="clipMini"><img src="${esc(thumb)}" onerror="this.style.display='none'"/><div><b>${esc(last.name||'Lanjut nonton')}</b><span>Lanjut: Ep.${ep}${last.dur?` / ${fmtTime(last.dur)}`:''}</span></div><button onclick="resumeWatch('${jsStr(last.id)}','${jsStr(thumb)}','${jsStr(last.plat)}',${ep})"><svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg></button><button class="mini-close" onclick="event.stopPropagation();$('#clipMini')?.remove()">×</button></div>`}
async function renderClips(){const box=$('#v-clips');if(!box)return;box.innerHTML=clipSkeleton();try{const d=await cachedJson(clipFeedUrl(P,1),180000);let items=flat(d.data??d).filter(x=>x&&x.drama_id&&x.thumb_url);if(!items.length)items=allItems.filter(x=>(x._p||platCache[x.drama_id]||P)===P&&x.thumb_url);items=items.slice(0,isPerformanceMode()?8:16);const chips=['Populer','CEO','Balas Dendam','Romantis','Korea','China','Comedy','Ongoing'];box.innerHTML=`<div class="clips-page"><section class="clips-hero"><div><span>Cuplikan</span><h1>${esc(platformLabel(P))} Picks</h1><p>Potongan pilihan buat cari tontonan cepat. Tap poster untuk detail, tombol play untuk langsung mulai episode pertama.</p></div><button onclick="renderClips()">Refresh</button></section><div class="clip-chip-row">${chips.map((q,i)=>`<button class="clip-chip ${i===0?'on':''}" onclick="${i===0?'renderClips()':`quickSearch('${jsStr(q)}')`}">${esc(q)}</button>`).join('')}</div><div class="clip-grid">${items.map(clipCardHtml).join('')}</div>${items.length?'':emptyHtml('Cuplikan belum tersedia','Coba pilih platform lain atau refresh')}</div>${clipMiniPlayerHtml()}`;}catch(e){ErrorLog.capture('clips','Gagal load cuplikan',{platform:P,error:String(e?.message||e)});box.innerHTML=errHtml(e?.message||e)}}

/* ===== RANDOM PICK ===== */
async function randomPick(){
  const platforms=['melolo','freereels','flickreels','dramanova','reelshort','netshort','dramabox','goodshort','moviebox','drakor'].filter(platformEnabled);
  const rp=platforms[Math.floor(Math.random()*platforms.length)];
  const base=API[rp];
  try{
    const pg=Math.floor(Math.random()*3)+1;
    let url=base+'/home?page='+pg;
    if(rp==='moviebox')url=base+'/indonesia?page='+pg+'&perPage=10';
    else if(rp==='drakor')url=base+'/home/korea?page='+pg+'&limit=30&sort=LATEST';
    else if(rp==='flickreels'||rp==='dramanova'||rp==='netshort'||rp==='goodshort')url=base+'/home';
    else if(rp==='dramabox')url=base+'/home?page='+pg+'&lang=in';
    else url=base+'/home?page='+pg+'&lang=id';
    const d=await cachedJson(url,180000);
    const items=flat(d.data);
    if(items.length){
      const pick=items[Math.floor(Math.random()*items.length)];
      if(pick.drama_id){platCache[pick.drama_id]=rp;openDet(pick.drama_id,pick.thumb_url)}
    }
  }catch(e){}
}

/* ===== SHARE ===== */
function shareDrama(){
  if(!curDrama)return;
  const title=cleanText(curDrama.drama_name||'Drama');
  const text=`Nonton "${title}" di Dramaku!`;
  if(nativeShare('Dramaku',text,location.href))return;
  if(navigator.share){navigator.share({title:'Dramaku',text,url:location.href}).catch(()=>{})}
  else{navigator.clipboard?.writeText(text+' '+location.href);toast('Link drama disalin')}
}

document.addEventListener('visibilitychange',()=>{if(document.hidden){$('#plOv')?.querySelectorAll('video').forEach(v=>v.pause())}});
