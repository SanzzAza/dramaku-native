/* Dramaku detail page */
function getDetailUrl(dp,id){
  if(dp==='dramabox')return API[dp]+`/detail?bookId=${id}&lang=in`;
  if(dp==='goodshort')return API[dp]+`/detail?bookId=${id}`;
  if(dp==='moviebox')return API[dp]+`/detail?subjectId=${id}`;
  if(dp==='drakor')return API[dp]+`/detail?id=${id}`;
  const nl=dp==='flickreels'||dp==='dramanova'||dp==='reelshort'||dp==='netshort';
  return API[dp]+`/detail?id=${id}`+(nl?'':'&lang=id');
}

async function resumeWatch(id,img,plat,ep,clip=false){
  curEps=[];
  if(img)thumbCache[id]=fixImg(img);if(plat)platCache[id]=plat;closeSearch();
  if(!clip){$('#detOv').classList.add('on');$('#detBody').innerHTML=detailLoadingHtml(img);document.body.style.overflow='hidden'}else{closeDet();toast('Memuat cuplikan Episode 1...')}
  const dp=plat||platCache[id]||P;
  try{
    const d=await cachedJson(getDetailUrl(dp,id),600000);if(dp==='drakor'?!d?.info:!d?.data)throw 0;
    let dd=dp==='drakor'?d.info:d.data;
    // GoodShort: data.book + data.list
    if(dp==='goodshort'&&d.data.book){dd={...d.data.book,drama_id:d.data.book.bookId,drama_name:d.data.book.bookName,description:d.data.book.introduction,episode_count:d.data.book.chapterCount,thumb_url:d.data.book.cover,tags:d.data.book.labels||[],watch_value:d.data.book.viewCountDisplay||''};curEps=d.data.list||[]}
    if(dp==='moviebox'){dd={drama_id:d.data.subjectId,drama_name:d.data.title,description:d.data.description||"",episode_count:d.data.subjectType===2?d.data.resourceDetectors?.[0]?.totalEpisode||1:1,thumb_url:d.data.cover?.url||"",tags:d.data.genre?d.data.genre.split(", "):[],watch_value:d.data.imdbRatingValue?"IMDb "+d.data.imdbRatingValue:"",_subjectType:d.data.subjectType};curEps=[]}
    if(dp==='drakor'&&d.info){const info=d.info,eps=d.episodes?.data||[];dd={...info,drama_id:info.id,drama_name:info.title,description:cleanText(info.meta_sinopsis||info.shoot||info.content||info.meta_description||''),episode_count:eps.length||info.meta_episode||0,thumb_url:info.image,tags:info.category?String(info.category).split(',').map(x=>x.trim()).filter(Boolean):[],watch_value:info.hits||'',_subjectType:2};curEps=eps}
    curDrama=dd;curDrama._p=dp;curDrama._thumb=fixImg(dd.thumb_url||dd.cover||dd.bookCover||thumbCache[id]||'');
    if(!curEps.length)curEps=dd.video_list||dd.episode_list||dd.episodes||dd.chapterList||[];
    if(!clip){renderDet(dd);setTimeout(()=>play(id,ep),300)}else{play(id,1,{clip:true})}
  }catch(e){ErrorLog.capture('detail',String(e?.message||e));$('#detBody').innerHTML=`<div style="padding-top:100px">${errHtml(e?.message||e)}</div>`}
}

async function openDet(id,img){
  curEps=[];
  if(img)thumbCache[id]=fixImg(img);closeSearch();
  $('#detOv').classList.add('on');$('#detBody').innerHTML=detailLoadingHtml(img);document.body.style.overflow='hidden';
  const dp=platCache[id]||P;
  try{
    const d=await cachedJson(getDetailUrl(dp,id),600000);if(dp==='drakor'?!d?.info:!d?.data)throw 0;
    let dd=dp==='drakor'?d.info:d.data;
    if(dp==='goodshort'&&d.data.book){dd={...d.data.book,drama_id:d.data.book.bookId,drama_name:d.data.book.bookName,description:d.data.book.introduction,episode_count:d.data.book.chapterCount,thumb_url:d.data.book.cover,tags:d.data.book.labels||[],watch_value:d.data.book.viewCountDisplay||''};curEps=d.data.list||[]}
    if(dp==='moviebox'){dd={drama_id:d.data.subjectId,drama_name:d.data.title,description:d.data.description||"",episode_count:d.data.subjectType===2?d.data.resourceDetectors?.[0]?.totalEpisode||1:1,thumb_url:d.data.cover?.url||"",tags:d.data.genre?d.data.genre.split(", "):[],watch_value:d.data.imdbRatingValue?"IMDb "+d.data.imdbRatingValue:"",_subjectType:d.data.subjectType};curEps=[]}
    if(dp==='drakor'&&d.info){const info=d.info,eps=d.episodes?.data||[];dd={...info,drama_id:info.id,drama_name:info.title,description:cleanText(info.meta_sinopsis||info.shoot||info.content||info.meta_description||''),episode_count:eps.length||info.meta_episode||0,thumb_url:info.image,tags:info.category?String(info.category).split(',').map(x=>x.trim()).filter(Boolean):[],watch_value:info.hits||'',_subjectType:2};curEps=eps}
    curDrama=dd;curDrama._p=dp;curDrama._thumb=fixImg(dd.thumb_url||dd.cover||dd.bookCover||thumbCache[id]||'');
    if(dd.thumb_url||dd.cover||dd.bookCover)thumbCache[id]=curDrama._thumb;
    if(!curEps.length)curEps=dd.video_list||dd.episode_list||dd.episodes||dd.chapterList||[];renderDet(dd);
  }catch(e){ErrorLog.capture('detail',String(e?.message||e));$('#detBody').innerHTML=`<div style="padding-top:100px">${errHtml(e?.message||e)}</div>`}
}
function getHistoryItem(id){return getHistory().find(x=>String(x.id)===String(id))||null}
function normalizedTags(d){return (Array.isArray(d?.tags)?d.tags:[]).map(t=>String(typeof t==='object'?(t.name||t.title||''):t).toLowerCase()).filter(Boolean)}
function getSimilarItems(d){const id=String(d?.drama_id||''),tags=normalizedTags(d),dp=d?._p||platCache[id]||P;let arr=allItems.filter(x=>String(x.drama_id)!==id);arr=arr.map(x=>{const xp=x._p||platCache[x.drama_id]||P,xt=normalizedTags(x);let score=xp===dp?2:0;score+=xt.filter(t=>tags.some(a=>a&&t.includes(a)||a.includes(t))).length*3;return{x,score}}).filter(o=>o.score>0).sort((a,b)=>b.score-a.score).map(o=>o.x);if(arr.length<6)arr=[...arr,...allItems.filter(x=>String(x.drama_id)!==id&&(x._p||platCache[x.drama_id]||P)===dp&&!arr.includes(x))];return arr.slice(0,isPerformanceMode()?6:10)}
function similarHtml(d){const items=getSimilarItems(d);if(!items.length)return'';return`<div class="similar-sec"><h3 class="detail-sec-title"><svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3 7h7l-5.5 4.5 2 7L12 16l-6.5 4.5 2-7L2 9h7z"/></svg>Mirip dengan ini</h3><div class="scroll-w"><div class="scroll-r">${items.map(cardHtml).join('')}</div></div></div>`}
function infoTile(label,value,icon){return`<div class="info-tile">${icon||''}<b>${esc(value||'-')}</b><span>${esc(label)}</span></div>`}
function renderDet(d){
  if(d&&d.drama_id){d._p=d._p||platCache[d.drama_id]||P;d.thumb_url=d.thumb_url||d._thumb||thumbCache[d.drama_id];rememberItems([d])}
  const ec=parseInt(d.episode_count||curEps.length||0)||0,total=Math.max(ec,1),did=jsStr(d.drama_id||'');
  const tagArr=Array.isArray(d.tags)?d.tags:(d.tags?[String(d.tags)]:['Drama']);
  const tags=tagArr.slice(0,5).map(t=>`<span class="d-tag">${esc(typeof t==='object'?(t.name||t.title||'Drama'):t)}</span>`).join('');
  const desc=String(d.description||'Belum ada sinopsis untuk judul ini.');
  const short=desc.length>170?desc.slice(0,170)+'...':desc;
  let eps='';for(let i=1;i<=total;i++)eps+=`<button class="ep-btn" onclick="play('${did}',${i})">${i}</button>`;
  const poster=d._thumb||fixImg(d.thumb_url||'')||thumbCache[d.drama_id]||'',r=ratingLabel(d),favOn=isFav(d.drama_id);
  const hist=getHistoryItem(d.drama_id),lastEp=parseInt(hist?.ep)||1,lastPct=Math.max(0,Math.min(100,parseInt(hist?.pct||0)||0));
  const resumeBlock=hist?`<div class="detail-panel resume-panel" onclick="play('${did}',${lastEp})"><div class="resume-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg></div><div class="resume-copy"><div class="resume-kicker">Lanjutkan tontonan</div><div class="resume-title">Episode ${lastEp}${lastPct?` · ${lastPct}%`:''}</div><div class="resume-sub">${hist.dur?`Terakhir di ${fmtTime(hist.pos||0)} / ${fmtTime(hist.dur||0)}`:'Mulai dari episode terakhir yang kamu buka'}</div>${lastPct?`<div class="resume-bar"><span style="width:${lastPct}%"></span></div>`:''}</div></div>`:'';
  const w=d.watch_value?fmtV(d.watch_value):'',wLabel=w?(String(w).toLowerCase().includes('imdb')?w:w+' views'):'';
  const status=total>1?'Serial':'Film';
  const platform=platformLabel(d._p||P);
  $('#detBody').innerHTML=`<div class="d-hero"><img src="${esc(poster)}" onerror="this.style.display='none'"/><div class="d-hero-grad"></div><button class="d-back" onclick="closeDet()" aria-label="Kembali"><svg width="17" height="17" fill="none" stroke="#fff" stroke-width="2.5" stroke-linecap="round"><path d="M14 8.5H4M8.5 14l-4.5-5.5L8.5 3"/></svg></button></div>
  <div class="d-info"><div class="d-poster"><img src="${esc(poster)}" alt="${esc(d.drama_name||'Poster')}" onerror="this.onerror=null;this.style.background='var(--bg3)'"/></div><div class="d-meta"><h1 class="d-title">${esc(d.drama_name||'Tanpa Judul')}</h1><div class="d-tags">${tags}</div><div class="d-stats">${r?`<span class="d-rating"><svg width="9" height="9" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3 7h7l-5.5 4.5 2 7L12 16l-6.5 4.5 2-7L2 9h7z"/></svg> ${esc(r)}</span>`:''}<span>${total} Episode</span>${wLabel?`<span>${esc(wLabel)}</span>`:''}<span>${esc(platform)}</span></div></div></div>
  <div class="d-actions">
    <button class="play-all-btn" style="flex:1" onclick="play('${did}',${hist?lastEp:1})"><svg width="18" height="18" viewBox="0 0 24 24" fill="#fff"><path d="M8 5v14l11-7z"/></svg> ${hist?'Lanjut Ep '+lastEp:'Mulai Tonton'}</button>
    <button class="start-over-btn" onclick="play('${did}',1)" aria-label="Mulai dari awal"><svg width="19" height="19" viewBox="0 0 24 24" fill="currentColor"><path d="M12 5V2L7 7l5 5V8c3.31 0 6 2.69 6 6a6 6 0 01-9.33 4.98l-1.42 1.42A8 8 0 1012 5z"/></svg></button>
    <button class="fav-btn ${favOn?'on':''}" style="background:${favOn?'var(--accent)':'var(--bg3)'}" onclick="toggleFavBtn(this)" aria-label="Simpan favorit"><svg width="20" height="20" viewBox="0 0 24 24" fill="#fff"><path d="M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z"/></svg></button>
    <button class="share-btn" onclick="shareDrama()" aria-label="Bagikan"><svg width="18" height="18" viewBox="0 0 24 24" fill="#fff"><path d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92s2.92-1.31 2.92-2.92-1.31-2.92-2.92-2.92z"/></svg></button>
  </div>
  ${resumeBlock}
  <div class="detail-panel"><div class="info-grid">${infoTile('Platform',platform,'<svg width="17" height="17" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2a10 10 0 100 20 10 10 0 000-20z"/></svg>')}${infoTile('Episode',total+' Ep','<svg width="17" height="17" viewBox="0 0 24 24" fill="currentColor"><path d="M4 6h16v12H4z"/></svg>')}${infoTile('Rating',r,'<svg width="17" height="17" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3 7h7l-5.5 4.5 2 7L12 16l-6.5 4.5 2-7L2 9h7z"/></svg>')}${infoTile('Tipe',status,'<svg width="17" height="17" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>')}</div></div>
  <div class="d-desc"><span id="dTxt">${esc(short)}</span>${desc.length>170?` <span class="more" onclick="togDesc()">Selengkapnya</span>`:''}</div>
  <div class="ep-sec"><div class="ep-sec-tt">Daftar Episode</div><div class="ep-grid">${eps}</div></div>
  ${similarHtml(d)}
  <div class="d-footer">Powered by <a href="#">Dramaku</a> &middot; Semua hak cipta milik pemiliknya</div>`;
}
function togDesc(){descOn=!descOn;const el=$('#dTxt'),tg=el.nextElementSibling,desc=String(curDrama?.description||'');if(descOn){el.textContent=desc;tg.textContent=' Sembunyikan'}else{el.textContent=desc.slice(0,150)+'...';tg.textContent=' Selengkapnya'}}
function closeDet(){$('#detOv').classList.remove('on');document.body.style.overflow='';curDrama=null;descOn=0}

