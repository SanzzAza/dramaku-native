/* Dramaku home, platforms, discovery cards */
function platformStatusHtml(){const keys=Object.keys(API);if(!keys.length)return'';const cards=keys.map(p=>{const st=platformState(p),enabled=platformEnabled(p);let status=st.status||(enabled?'active':'maintenance'),reason=st.reason||(enabled?'Siap dipakai':'Sedang maintenance');try{const host=(API[p]||'').match(/^https?:\/\/[^/]+/);const h=host&&apiHealth[host[0]];if(h&&h.fail>h.ok&&Date.now()-(h.lastAt||0)<10*60*1000){status='slow';reason=h.lastError?('API: '+h.lastError):'API lambat/gagal terakhir'}}catch(_){}const cls=enabled?(status==='slow'?'slow':'ok'):'off',ico=enabled?(status==='slow'?'🟡':'🟢'):'🔴';return`<div class="plat-status-card ${cls}" onclick="${enabled?`setPlatform('${p}')`:`toast('${platformLabel(p)}: ${jsStr(reason)}')`}"><b>${ico} ${esc(platformLabel(p))}</b><span>${esc(reason)}</span></div>`}).join('');return`<section class="platform-status"><div class="sec-hd" style="padding:0 16px;margin-bottom:10px"><h2 class="sec-tt">Status Platform</h2><div class="sec-more" onclick="reloadRemoteConfig()">Refresh</div></div><div class="plat-status-scroll">${cards}</div></section>`}
function togglePlat(){$('#platDd').classList.toggle('on');$('#platBtn').classList.toggle('open')}
function setPlatform(p){
  if(!platformEnabled(p)){toast(platformLabel(p)+' nonaktif: '+platformReason(p));return}
  P=p;$('#platLabel').textContent=platformLabel(p);try{localStorage.setItem('dk_platform',p)}catch(e){};bumpEl($('#platBtn'));haptic('light')
  $$('.plat-opt').forEach(e=>e.classList.toggle('on',e.dataset.p===p));$('#platDd').classList.remove('on');$('#platBtn').classList.remove('open');
  // Dynamic tabs per platform
  const tabEl=document.querySelector('.tabs');
  const defTabs='<div class="tab on" data-t="home" onclick="go(\'home\')">Beranda</div><div class="tab" data-t="populer" onclick="go(\'populer\')">Populer</div><div class="tab" data-t="new" onclick="go(\'new\')">Terbaru</div>';
  if(p==='moviebox'){
    tabEl.innerHTML='<div class="tab on" data-t="home" onclick="go(\'home\')">Indonesia</div><div class="tab" data-t="populer" onclick="go(\'populer\')">Global</div><div class="tab" data-t="new" onclick="go(\'new\')">Horror</div><div class="tab" data-t="t4" onclick="go(\'t4\')">Asia</div><div class="tab" data-t="t5" onclick="go(\'t5\')">Anime</div><div class="tab" data-t="t6" onclick="go(\'t6\')">CDrama</div><div class="tab" data-t="t7" onclick="go(\'t7\')">Reality</div>';
  }else if(p==='dramabox'){
    tabEl.innerHTML='<div class="tab on" data-t="home" onclick="go(\'home\')">Beranda</div><div class="tab" data-t="populer" onclick="go(\'populer\')">Populer</div><div class="tab" data-t="new" onclick="go(\'new\')">Terbaru</div><div class="tab" data-t="t4" onclick="go(\'t4\')">Ranking</div><div class="tab" data-t="t5" onclick="go(\'t5\')">Kategori</div><div class="tab" data-t="t6" onclick="go(\'t6\')">Cina</div><div class="tab" data-t="t7" onclick="go(\'t7\')">Korea</div>';
  }else if(p==='drakor'){
    tabEl.innerHTML='<div class="tab on" data-t="home" onclick="go(\'home\')">Korea</div><div class="tab" data-t="populer" onclick="go(\'populer\')">Trending</div><div class="tab" data-t="new" onclick="go(\'new\')">Terbaru</div><div class="tab" data-t="t4" onclick="go(\'t4\')">China</div><div class="tab" data-t="t5" onclick="go(\'t5\')">Ongoing</div><div class="tab" data-t="t6" onclick="go(\'t6\')">Comedy</div>';
  }else{
    tabEl.innerHTML=defTabs;
  }
  resetState();go('home');
}
document.addEventListener('click',e=>{if(!e.target.closest('.plat-sel')){$('#platDd').classList.remove('on');$('#platBtn').classList.remove('open')}});
document.addEventListener('keydown',e=>{if(e.key==='Escape'){if(!handleNativeBack()){$('#platDd')?.classList.remove('on');$('#platBtn')?.classList.remove('open')}}});

function fixImg(u){if(!u)return'';if(u.includes('fizzopic.org')&&u.includes('.heic')){const m=u.match(/novel-images-apsoutheast\/([a-f0-9]+)~/);if(m&&m[1])return'https://p19-novel-sg.ibyteimg.com/img/novel-images-sg/'+m[1]+'~tplv-resize:570:810.jpg'}return u}
function normalizeDramaBoxBook(d){return{drama_id:String(d.bookId||d.drama_id||''),drama_name:d.bookName||d.drama_name||d.title||'',description:d.introduction||d.description||'',episode_count:d.chapterCount||d.episode_count||'',watch_value:d.rankVo?.hotCode||d.watch_value||d.hotCode||'',thumb_url:d.coverWap||d.thumb_url||d.cover||'',tags:Array.isArray(d.tags)?d.tags:(d.tagV3s?d.tagV3s.map(t=>t.tagName).filter(Boolean):[]),is_new_book:'0',_p:'dramabox',_raw:d}}
function flat(data){
  if(!data||typeof data!=='object')return[];let out=[];
  if(Array.isArray(data)){data.forEach(g=>{if(g.books&&Array.isArray(g.books))out.push(...g.books);else if(g.drama_id)out.push(g);else if(g.bookId)out.push(normalizeDramaBoxBook(g));else if(g.id&&g.title)out.push({drama_id:g.id,drama_name:g.title,description:cleanText(g.meta_description||g.shoot||''),episode_count:g.meta_episode||g.episode_number||'',watch_value:g.hits||'',thumb_url:g.image||'',tags:g.category?String(g.category).split(',').map(x=>x.trim()).filter(Boolean):[],is_new_book:'0',_p:'drakor',_raw:g})})}
  else if(data.trending||data.popular||data.newest){const seen=new Set();['trending','popular','newest'].forEach(k=>(data[k]||[]).forEach(d=>{const id=String(d.bookId||'');if(id&&!seen.has(id)){seen.add(id);out.push(normalizeDramaBoxBook(d))}}))}
  else if(data.classifyBookList?.records&&Array.isArray(data.classifyBookList.records)){out=data.classifyBookList.records.map(normalizeDramaBoxBook)}
  else if(data.items&&Array.isArray(data.items)){out=data.items.map(d=>({drama_id:d.drama_id,drama_name:d.title||d.drama_name||'',description:d.description||d.synopsis||'',episode_count:d.total_episodes||d.episode_count||'',watch_value:d.view_count?String(d.view_count):'',thumb_url:d.poster||d.raw?.coverImage||d.raw?.posterImg||'',tags:d.categories?d.categories.map(c=>c.name||c):(d.raw?.categoryNames||[]),free:d.free||false,is_new_book:d.is_new_book||'0'}))}
  // MovieBox: data.subjects[] or data.results[].subjects[]
  else if(data.subjects&&Array.isArray(data.subjects)){out=data.subjects.map(d=>({drama_id:d.subjectId,drama_name:d.title||'',description:d.description||'',episode_count:'',watch_value:d.viewers?String(d.viewers):'',thumb_url:d.cover?.url||'',tags:d.genre?d.genre.split(', '):[],is_new_book:'0',_subjectType:d.subjectType}))}
  else if(data.results&&Array.isArray(data.results)){data.results.forEach(r=>{if(r.subjects)r.subjects.forEach(d=>out.push({drama_id:d.subjectId,drama_name:d.title||'',description:d.description||'',episode_count:'',watch_value:'',thumb_url:d.cover?.url||'',tags:d.genre?d.genre.split(', '):[],is_new_book:'0',_subjectType:d.subjectType}))})}
  out.forEach(d=>{if(d.thumb_url)d.thumb_url=fixImg(d.thumb_url);if(!d.thumb_url&&d.cover)d.thumb_url=d.cover;const dp=d.free?'freereels':(d._p||P);if(d.drama_id){d._p=dp;platCache[d.drama_id]=dp;if(d.thumb_url)thumbCache[d.drama_id]=d.thumb_url}});rememberItems(out);
  return out;
}


let ptrBound=false, ptrState={pulling:false,startY:0,dy:0};
function bindPullToRefresh(){
  const root=$('#ptrRoot'), ind=$('#ptrInd');
  if(!root||!ind)return;
  // rebind each home render
  const content=root.querySelector('.ptr-content');
  if(!content)return;
  const onStart=(e)=>{
    if(curTab!=='home')return;
    if(window.scrollY>8)return;
    const y=e.touches?e.touches[0].clientY:e.clientY;
    ptrState={pulling:true,startY:y,dy:0};
  };
  const onMove=(e)=>{
    if(!ptrState.pulling)return;
    if(window.scrollY>8){ptrState.pulling=false;root.style.transform='';ind.classList.remove('on','ready');return}
    const y=e.touches?e.touches[0].clientY:e.clientY;
    let dy=Math.max(0,y-ptrState.startY);
    if(dy<=0)return;
    if(dy>120)dy=120;
    ptrState.dy=dy;
    root.style.transition='none';
    root.style.transform=`translateY(${dy*0.45}px)`;
    ind.classList.add('on');
    ind.classList.toggle('ready', dy>70);
    ind.querySelector('.ptr-text').textContent=dy>70?'Lepas untuk refresh':'Tarik untuk refresh';
    if(dy>20&&e.cancelable)e.preventDefault();
  };
  const onEnd=async()=>{
    if(!ptrState.pulling)return;
    const should=ptrState.dy>70;
    ptrState.pulling=false;ptrState.dy=0;
    root.style.transition='transform .25s cubic-bezier(.16,1,.3,1)';
    if(should){
      ind.classList.add('ready');
      ind.querySelector('.ptr-text').textContent='Menyegarkan...';
      root.style.transform='translateY(42px)';
      haptic('light');
      try{await refreshHome()}catch(e){}
      root.style.transform='';
      ind.classList.remove('on','ready');
    }else{
      root.style.transform='';
      ind.classList.remove('on','ready');
    }
  };
  // clone node to drop old listeners simply by replacing handlers via property once markers
  if(root._ptrBound){
    root.removeEventListener('touchstart', root._ptrStart, {passive:true});
    root.removeEventListener('touchmove', root._ptrMove, {passive:false});
    root.removeEventListener('touchend', root._ptrEnd);
  }
  root._ptrStart=onStart; root._ptrMove=onMove; root._ptrEnd=onEnd; root._ptrBound=true;
  root.addEventListener('touchstart', onStart, {passive:true});
  root.addEventListener('touchmove', onMove, {passive:false});
  root.addEventListener('touchend', onEnd);
}
async function refreshHome(){
  try{
    // clear home api cache keys lightly
    Object.keys(jsonMemCache).forEach(k=>{if(String(k).includes('/home')||String(k).includes('/populer')||String(k).includes('/new')||String(k).includes('/recommend')||String(k).includes('/discovery')||String(k).includes('/indonesia')||String(k).includes('/trending'))delete jsonMemCache[k]});
  }catch(e){}
  loaded.home=0; busy.home=0; more.home=1; pg.home=1;
  await loadTab('home');
  toast('Beranda disegarkan');
}

function go(t){curTab=t;$$('.tab').forEach(e=>{const on=e.dataset.t===t;e.classList.toggle('on',on);if(on)bumpEl(e)});$$('.bnav-item').forEach(e=>{const n=e.dataset.n;const on=n===t||(n==='profile'&&['profile','settings','fav'].includes(t));e.classList.toggle('on',on);if(on)bumpEl(e)});['home','clips','populer','new','t4','t5','t6','t7','history','fav','settings','profile'].forEach(k=>{const el=$('#v-'+k);if(el)el.style.display=k===t?'block':'none'});
  if(t==='history'){renderHistory()}else if(t==='fav'){renderFav()}else if(t==='settings'){renderSettings()}else if(t==='profile'){renderProfile()}else if(t==='clips'){renderClips()}else if(!$('#v-'+t).innerHTML.trim())loadTab(t);window.scrollTo(0,0)}
function goHome(){closeDet();closePl();go('home')}

async function loadTab(t){
  if(busy[t]||more[t]===0)return;busy[t]=1;const box=$('#v-'+t),base=API[P];
  try{
  if(!base){if(box)box.innerHTML=errHtml('Endpoint platform tidak tersedia');return}
  if(t==='home'&&!loaded.home){
    box.innerHTML=skelHtml(9);
    try{
      const nl=P==='flickreels'||P==='dramanova'||P==='reelshort'||P==='netshort';
      const dbLang=P==='dramabox'?'&lang=in':(!nl?'&lang=id':'');
      let hU=base+'/home?page=1'+dbLang,pU=base+'/populer?page=1'+dbLang,nU=base+'/new?page=1'+dbLang;
      if(P==='dramanova'){pU=base+'/discovery?size=10';nU=base+'/recommend?page=1&size=10'}
      else if(P==='flickreels'){pU=base+'/populer';nU=base+'/new?page=1'}
      else if(P==='reelshort'){hU=base+'/home?tab_id=0&sub_tab_id=0';pU=base+'/populer?page=1&limit=20&period=0&rule=0';nU=base+'/new?page=1&limit=20'}
      else if(P==='netshort'){hU=base+'/home?page=1';pU=base+'/populer';nU=base+'/new'}
      else if(P==='dramabox'){hU=base+'/home?page=1&lang=in';pU=base+'/populer?page=1&lang=in';nU=base+'/new?page=1&lang=in'}
      else if(P==='goodshort'){hU=base+'/home';pU=base+'/populer?page=1';nU=base+'/new?page=1&channelId=563'}
      else if(P==='moviebox'){hU=base+'/indonesia?page=1&perPage=10';pU=base+'/indonesia?page=1&perPage=10';nU=base+'/global?page=1&perPage=10'}
      else if(P==='drakor'){hU=base+'/home/korea?page=1&limit=30&sort=LATEST';pU=base+'/trending?page=1&limit=30&days=30';nU=base+'/terbaru?page=1&limit=30'}
      const settled=await Promise.allSettled([cachedJson(hU,180000),cachedJson(pU,180000),cachedJson(nU,180000)]);
      const unwrap=(s,i)=>{if(s.status==='fulfilled')return s.value;ErrorLog.capture('home_part',String(s.reason?.message||s.reason||'fail'),{platform:P,part:i});return null};
      const hd=unwrap(settled[0],'home'),pd=unwrap(settled[1],'populer'),nd=unwrap(settled[2],'new');
      if(!hd&&!pd&&!nd)throw new Error(navigator.onLine?'Semua endpoint gagal/timeout':'Offline dan cache kosong');
      const perf=isPerformanceMode();const pop=flat(pd?.data??pd).slice(0,perf?6:10),nw=flat(nd?.data??nd).slice(0,perf?8:12),rec=flat(hd?.data??hd).slice(0,perf?8:18);
      let h='';
      const spotlightPool=[...pop,...nw,...rec];
      const hr=new Date().getHours();const greet=hr<11?'Selamat pagi':hr<15?'Selamat siang':hr<18?'Selamat sore':'Selamat malam';
      h+=spotlightHtml(spotlightPool);
      if(pop.length)h+=top10Html(pop,'Top 10 Hari Ini');
      h+=discoverHtml();
      h+=continueWatchingHtml(getHistory(),greet);
      h+=forYouHtml(spotlightPool);
      h+=platformStatusHtml();
      if(nw.length)h+=secHtml('Drama Terbaru',nw,'new',1);
      if(rec.length)h+=`<div class="sec"><div class="sec-hd"><h2 class="sec-tt">Rekomendasi</h2></div><div class="grid">${rec.map(cardHtml).join('')}</div></div>`;
      h+=`<div class="home-footer">Dramaku v${APP_VERSION} · 10 Platform · Dibuat dengan cinta<br>Semua konten milik platform masing-masing</div>`;
      if(!pop.length&&!nw.length&&!rec.length){box.innerHTML=errHtml('Data platform kosong atau gagal dimuat');}
      else{box.innerHTML=`<div class="ptr-root" id="ptrRoot"><div class="ptr-indicator" id="ptrInd"><span class="ptr-spinner"></span><span class="ptr-text">Tarik untuk refresh</span></div><div class="ptr-content">${h||errHtml()}</div></div>`;loaded.home=1;bindPullToRefresh();if([hd,pd,nd].filter(Boolean).length<3)toast('Sebagian data gagal dimuat');}
    }catch(e){ErrorLog.capture('home',String(e?.message||e),{platform:P});box.innerHTML=errHtml(e?.message||e)}
  }else if(t!=='home'){
    const tabNames={populer:'Paling Populer',new:'Paling Baru',t4:'',t5:'',t6:'',t7:''};
    const activeTab=document.querySelector(`.tab[data-t="${t}"]`);
    const secTitle=activeTab?activeTab.textContent.trim():tabNames[t]||'';
    if(pg[t]===1)box.innerHTML=`<div class="sec"><div class="sec-hd"><h2 class="sec-tt">${secTitle}</h2></div><div class="grid" id="g-${t}"></div></div><div id="trg-${t}"></div>`;
    const g=$('#g-'+t);if(pg[t]===1&&g)g.innerHTML=skelHtml(9,1);
    try{
      let ep,qp;
      if(P==='dramanova'){ep=t==='populer'?'/discovery':'/recommend';qp=t==='populer'?`?size=10&page=${pg[t]}`:`?page=${pg[t]}&size=10`}
      else if(P==='flickreels'){ep=t==='populer'?'/populer':'/new';qp=t==='populer'?'':`?page=${pg[t]}`}
      else if(P==='reelshort'){ep=t==='populer'?'/populer':'/new';qp=t==='populer'?`?page=${pg[t]}&limit=20&period=0&rule=0`:`?page=${pg[t]}&limit=20`}
      else if(P==='netshort'){ep=t==='populer'?'/populer':'/new';qp=''}
      else if(P==='dramabox'){
        const dbMap={home:'/home',populer:'/populer',new:'/new',t4:'/rank',t5:'/category',t6:'/category?category=cina',t7:'/category?category=korea'};
        ep=dbMap[t]||'/home';qp=(t==='t4'?'?lang=in':(t==='t6'||t==='t7'?'&page='+pg[t]+'&lang=in':'?page='+pg[t]+'&lang=in'));
      }
      else if(P==='goodshort'){ep=t==='populer'?'/populer':'/new';qp=t==='populer'?`?page=${pg[t]}`:`?page=${pg[t]}&channelId=563`}
      else if(P==='drakor'){const dkMap={home:'/home/korea',populer:'/trending',new:'/terbaru',t4:'/home/china',t5:'/ongoing',t6:'/category'};ep=dkMap[t]||'/home/korea';qp=t==='populer'?`?page=${pg[t]}&limit=30&days=30`:(t==='new'||t==='t5'?`?page=${pg[t]}&limit=30`:(t==='t6'?`?category_name=Comedy&page=${pg[t]}&limit=30&sort=LATEST`:`?page=${pg[t]}&limit=30&sort=LATEST`))}
      else if(P==='moviebox'){
        const mbMap={home:'/indonesia',populer:'/global',new:'/horror',t4:'/asia',t5:'/series/anime',t6:'/series/cdrama',t7:'/series/reality'};
        ep=mbMap[t]||'/indonesia';qp=`?page=${pg[t]}&perPage=10`;
      }
      else{ep=t==='populer'?'/populer':'/new';qp=`?page=${pg[t]}&lang=id`}
      const d=await cachedJson(base+ep+qp,180000);const items=flat(d.data??d);
      if(pg[t]===1&&g)g.innerHTML='';
      if(items.length){if(g)g.insertAdjacentHTML('beforeend',items.map(cardHtml).join(''));pg[t]++;if(d.has_more===false||items.length<8||(P==='dramanova'&&t==='populer')||(P==='flickreels'&&t==='populer')||(P==='dramabox'&&['t4','t5','t6','t7'].includes(t)))more[t]=0}else more[t]=0;
    }catch(e){ErrorLog.capture('tab',String(e?.message||e),{tab:t,platform:P});if(pg[t]===1&&g)g.innerHTML=errHtml(e?.message||e);else toast('Gagal memuat lanjutan')}
  }
  }catch(e){ErrorLog.capture('tab_fatal',String(e?.message||e),{tab:t,platform:P});try{if(box)box.innerHTML=errHtml(e?.message||e)}catch(_){}}
  finally{busy[t]=0}
}



function tokenizeInterest(s){
  return String(s||'').toLowerCase()
    .replace(/[^a-z0-9\u00c0-\u024f\u0400-\u04ff\u4e00-\u9fff\s]/gi,' ')
    .split(/\s+/).filter(w=>w.length>2 && !/^(the|and|untuk|dengan|yang|dari|this|drama|episode|full|indo|sub)$/.test(w));
}
function forYouHtml(pool){
  const hist=typeof getHistory==='function'?getHistory():[];
  if(!hist.length||!pool||!pool.length)return'';
  const seen=new Set(hist.map(h=>String(h.id)));
  const bag={};
  hist.slice(0,12).forEach(h=>{
    tokenizeInterest(h.name).forEach(w=>{bag[w]=(bag[w]||0)+2});
    if(h.plat)bag['plat:'+h.plat]=(bag['plat:'+h.plat]||0)+3;
  });
  const scored=(pool||[]).filter(d=>d&&d.drama_id&&!seen.has(String(d.drama_id))).map(d=>{
    let s=0;
    const title=tokenizeInterest(d.drama_name||d.title||'');
    title.forEach(w=>{if(bag[w])s+=bag[w]});
    const pl=d._p||platCache[d.drama_id]||'';
    if(bag['plat:'+pl])s+=bag['plat:'+pl];
    if(extractRealRating(d))s+=1;
    if(d.thumb_url)s+=1;
    return {d,s};
  }).filter(x=>x.s>0).sort((a,b)=>b.s-a.s);
  let picks=scored.slice(0,isPerformanceMode()?6:9).map(x=>x.d);
  if(picks.length<3){
    // fallback: same platforms as history
    const plats=new Set(hist.map(h=>h.plat).filter(Boolean));
    const extra=(pool||[]).filter(d=>d&&d.drama_id&&!seen.has(String(d.drama_id))&&plats.has(d._p||platCache[d.drama_id]));
    picks=[...picks,...extra].filter((d,i,arr)=>arr.findIndex(x=>String(x.drama_id)===String(d.drama_id))===i).slice(0,isPerformanceMode()?6:9);
  }
  if(picks.length<3)return'';
  const chips=Object.entries(bag).filter(([k,v])=>!k.startsWith('plat:')&&v>=2).sort((a,b)=>b[1]-a[1]).slice(0,4).map(([k])=>k);
  return`<section class="sec foryou-sec"><div class="sec-hd"><h2 class="sec-tt">Buat Kamu</h2><div class="sec-more" onclick="go('history')">Dari riwayat</div></div>${chips.length?`<div class="foryou-chips">${chips.map(c=>`<span class="fy-chip">#${esc(c)}</span>`).join('')}</div>`:''}<div class="grid">${picks.map(cardHtml).join('')}</div></section>`;
}

function spotlightHtml(items){
  const pool=(items||[]).filter(d=>d&&d.drama_id&&d.thumb_url);
  if(!pool.length)return '';
  const pick=pool[(new Date().getDate()+pool.length)%pool.length];
  const img=fixImg(pick.thumb_url||''),id=String(pick.drama_id||''),nm=pick.drama_name||'Pilihan Dramaku',desc=pick.description||'Pilihan tontonan yang lagi cocok buat kamu hari ini.';
  const pl=pick._p||platCache[id]||P,ep=pick.episode_count||'',sid=jsStr(id),si=jsStr(img),r=ratingLabel(pick);
  const tag=(Array.isArray(pick.tags)?pick.tags:[]).map(t=>typeof t==='object'?(t.name||t.title||''):t).filter(Boolean)[0]||platformLabel(pl);
  return`<div class="spotlight-wrap"><section class="spotlight-card" onclick="openDet('${sid}','${si}')" aria-label="Spotlight ${esc(nm)}"><img class="spot-bg" src="${esc(img)}" alt="" loading="lazy" decoding="async"><div class="spot-poster"><img src="${esc(img)}" alt="${esc(nm)}" loading="lazy" decoding="async"></div><div class="spot-info"><div class="spot-kicker"><span class="live-dot"></span> Spotlight Hari Ini</div><div class="spot-title">${esc(nm)}</div><div class="spot-desc">${esc(desc)}</div><div class="spot-meta">${r?`<span class="spot-pill">⭐ ${esc(r)}</span>`:''}${ep?`<span class="spot-pill">${esc(ep)} Ep</span>`:''}<span class="spot-pill">${esc(tag)}</span></div><div class="spot-actions"><button class="spot-play" onclick="event.stopPropagation();openDet('${sid}','${si}')"><svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>Tonton</button><button class="spot-more" onclick="event.stopPropagation();randomPick()">Coba Lain</button></div></div></section></div>`;
}
function top10Html(items,title='Top 10 Hari Ini'){
  const list=(items||[]).filter(d=>d&&d.drama_id&&d.thumb_url).slice(0,isPerformanceMode()?6:10);
  if(!list.length)return'';
  return`<section class="top10-section"><div class="sec-hd" style="padding:0 16px;margin-bottom:10px"><h2 class="sec-tt">${esc(title)}</h2><div class="sec-more" onclick="go('populer')">Ranking</div></div><div class="top10-row">${list.map((d,i)=>{const img=fixImg(d.thumb_url||''),id=jsStr(d.drama_id),si=jsStr(img),nm=d.drama_name||'';return`<article class="top10-card" onclick="openDet('${id}','${si}')"><div class="top10-num">${i+1}</div><div class="top10-poster"><img src="${esc(img)}" alt="${esc(nm)}" loading="lazy" decoding="async" onerror="this.style.display='none'"/></div><div class="top10-info"><b>${esc(nm)}</b><span>${esc(platformLabel(d._p||platCache[d.drama_id]||P))}${d.episode_count?` · ${esc(d.episode_count)} Ep`:''}</span></div></article>`}).join('')}</div></section>`
}
function continueWatchingHtml(history,greet='Lanjut nonton?'){
  const list=(history||[]).slice(0,isPerformanceMode()?5:8);
  if(!list.length)return'';
  return`<section class="continue-sec"><div class="sec-hd" style="padding:0 16px;margin-bottom:10px"><h2 class="sec-tt">Lanjutkan Tontonan</h2><div class="sec-more" onclick="go('history')">Semua</div></div><div class="cw-row">${list.map((d,idx)=>{const thumb=fixImg(d.thumb||''),ep=parseInt(d.ep)||1,pct=Math.max(0,Math.min(100,parseInt(d.pct||0)||0));return`<article class="cw-item" onclick="resumeWatch('${jsStr(d.id)}','${jsStr(thumb)}','${jsStr(d.plat)}',${ep})"><div class="cw-item-poster"><img src="${esc(thumb)}" alt="${esc(d.name)}" loading="lazy" decoding="async" onerror="this.style.display='none'"/><div class="cw-item-badge">Ep ${ep}</div>${pct?`<div class="cw-item-progress"><span style="width:${pct}%"></span></div>`:''}${pct?progressRingHtml(pct,32):''}</div><div class="cw-item-copy"><b>${esc(d.name||'Tanpa Judul')}</b><span>${idx===0?esc(greet):'Lanjut'}${pct?` · ${pct}%`:''}</span></div></article>`}).join('')}</div></section>`
}
function moodHtml(){
  // Replaces old mood grid with a more useful "Discover" strip:
  // quick actions + viral chips (search shortcuts).
  return discoverHtml();
}
function discoverHtml(){
  const actions=[
    ['🎲','Acak','Temukan drama','randomPick()','rgba(16,245,166,.16)'],
    ['▶','Cuplikan','Trailer cepat',"go('clips')",'rgba(251,113,133,.14)'],
    ['⏱','Riwayat','Lanjut nonton',"go('history')",'rgba(96,165,250,.14)'],
    ['♥','Favorit','Koleksi kamu',"go('fav')",'rgba(244,114,182,.14)']
  ];
  // Mix static viral + recent searches for personal feel
  let viral=['CEO','Balas Dendam','Romantis','Korea','China','Isekai','Cinta Kontrak','Action','Comedy','Drakor'];
  try{
    const rec=(typeof getRecentSearches==='function'?getRecentSearches():[]).slice(0,4);
    if(rec.length){
      const merged=[];
      rec.forEach(q=>{if(q&&!merged.some(x=>x.toLowerCase()===String(q).toLowerCase()))merged.push(String(q))});
      viral.forEach(v=>{if(!merged.some(x=>x.toLowerCase()===v.toLowerCase()))merged.push(v)});
      viral=merged.slice(0,10);
    }
  }catch(e){}
  const act=actions.map(([icon,title,sub,fn,bg])=>
    `<button type="button" class="disc-action" style="--da:${bg}" onclick="${fn};bumpEl(this);haptic('light')">
      <span class="da-ico">${icon}</span>
      <span class="da-copy"><b>${title}</b><small>${sub}</small></span>
    </button>`
  ).join('');
  const chips=viral.map((q,i)=>
    `<button type="button" class="viral-chip ${i===0?'hot':''}" onclick="quickSearch('${jsStr(q)}');haptic('light')">${i===0?'🔥 ':''}${esc(q)}</button>`
  ).join('');
  return `<section class="discover-sec">
    <div class="sec-hd" style="padding:0 16px;margin-bottom:10px">
      <h2 class="sec-tt">Jelajah Cepat</h2>
      <div class="sec-more" onclick="openSearch()">Cari</div>
    </div>
    <div class="disc-actions">${act}</div>
    <div class="viral-head"><span>Lagi viral</span><span class="viral-hint">tap untuk cari</span></div>
    <div class="viral-row">${chips}</div>
  </section>`;
}
function cardHtml(d){
  const img=fixImg(d.thumb_url||''),nm=d.drama_name||'',ep=d.episode_count||'',id=String(d.drama_id||''),vw=d.watch_value||'',isFree=d.free===true;
  const pl=platCache[id]||d._p||P,si=jsStr(img),sid=jsStr(id);
  const wm=watchMetaFor(id);
  const views=vw&&vw!=='0'&&!/imdb/i.test(String(vw))?`<div class="badge-views">${esc(fmtV(vw))}</div>`:'';
  const cont=wm&&(wm.pct>0||wm.ep>1)?`<div class="badge-cont">LANJUT${wm.pct?` ${wm.pct}%`:''}</div>`:'';
  const neu=!cont&&isFreshItem(d)?'<div class="badge-new">BARU</div>':'';
  const prog=wm&&wm.pct?`<div class="card-progress"><span style="width:${wm.pct}%"></span></div>`:'';
  const ring=wm&&wm.pct?progressRingHtml(wm.pct,30):'';
  return`<article class="card" role="button" tabindex="0" onclick="openDet('${sid}','${si}')" onkeydown="if(event.key==='Enter'||event.key===' '){event.preventDefault();openDet('${sid}','${si}')}" aria-label="Buka ${esc(nm)}"><div class="card-img"><img src="${esc(img)}" alt="${esc(nm)}" loading="lazy" decoding="async" onerror="this.onerror=null;this.style.display='none'"/>${ep&&ep!=='0'&&ep!==0?`<div class="badge-ep">${esc(ep)} Ep</div>`:''}${isFree?'<div class="badge-free">FREE</div>':''}${views}${cont}${neu}<div class="badge-plat">${esc(platformLabel(pl))}</div>${prog}${ring}</div><div class="card-body"><div class="card-name">${esc(nm)}</div>${ratingBadgeHtml(d)}${wm?`<div class="card-sub">Ep ${wm.ep}${wm.pct?` · ${wm.pct}%`:''}</div>`:''}</div></article>`;
}
function fmtV(v){if(!v)return'';const n=parseInt(v);if(isNaN(n))return v;if(n>=1e6)return(n/1e6).toFixed(1)+'M';if(n>=1e3)return(n/1e3).toFixed(1)+'K';return v}
function secHtml(title,items,tab,scroll){
  const cards=items.map(cardHtml).join('');
  return`<div class="sec"><div class="sec-hd"><h2 class="sec-tt">${title}</h2><div class="sec-more" onclick="go('${tab}')">Semua <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6"/></svg></div></div>${scroll?`<div class="scroll-w"><div class="scroll-r">${cards}</div></div>`:`<div class="grid">${cards}</div>`}</div>`;
}
function skelHtml(n,f){let s='';for(let i=0;i<n;i++)s+=`<div class="skel-card"><div class="skel skel-img"></div><div class="skel-body"><div class="skel skel-t"></div><div class="skel skel-t2"></div><div class="skel skel-pill"></div></div></div>`;return f?s:`<div class="home-loading"><div class="skel-hero"><div class="skel skel-hero-icon"></div><div class="skel-hero-lines"><div class="skel skel-line big"></div><div class="skel skel-line"></div><div class="skel skel-line short"></div></div></div><div class="sec"><div class="sec-hd"><div class="skel skel-sec-title"></div><div class="skel skel-sec-more"></div></div><div class="grid">${s}</div></div></div>`}
function detailLoadingHtml(img=''){const poster=fixImg(img||'');return`<div class="detail-loading">${poster?`<img class="dl-bg" src="${esc(poster)}" alt=""/>`:''}<div class="dl-card"><div class="skel dl-poster"></div><div class="dl-copy"><div class="skel dl-title"></div><div class="skel dl-line"></div><div class="skel dl-line short"></div><div class="dl-actions"><div class="skel dl-btn"></div><div class="skel dl-round"></div><div class="skel dl-round"></div></div></div></div><div class="load-spin"><div class="spinner"></div></div></div>`}
function errHtml(detail){
  const offline=!navigator.onLine;
  const title=offline?'Kamu sedang offline':'Gagal memuat data';
  const sub=offline
    ?'Sambungkan internet, lalu coba lagi. Cache lokal akan dipakai otomatis jika ada.'
    :(detail?String(detail):'Server platform lambat atau tidak merespons. Coba lagi sebentar.');
  return`<div class="empty-state"><svg width="56" height="56" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg><p>${esc(title)}</p><p class="empty-sub">${esc(sub)}</p><div class="empty-actions"><button class="retry" onclick="retry()">Coba Lagi</button><button class="retry retry-ghost" onclick="clearApiCache();retry()">Bersihkan Cache</button></div></div>`
}
function emptyHtml(msg,sub){return`<div class="empty-state"><svg width="56" height="56" viewBox="0 0 24 24" fill="currentColor"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H5.17L4 17.17V4h16v12z"/><path d="M12 12h2v2h-2zm0-6h2v4h-2z"/></svg><p>${esc(msg)}</p>${sub?`<p class="empty-sub">${esc(sub)}</p>`:''}</div>`}
function retry(){pg[curTab]=1;more[curTab]=1;busy[curTab]=0;delete loaded[curTab];const box=$('#v-'+curTab);if(box)box.innerHTML='';loadTab(curTab)}

/* Lanjut nonton — buka detail lalu langsung play */
