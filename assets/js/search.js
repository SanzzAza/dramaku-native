/* Dramaku search */
function searchEmptyHtml(){const chips=['CEO','Balas Dendam','Romantis','Korea','China','Comedy','Cinta','Ongoing'];return '<div class="empty-state search-welcome" style="padding-top:72px"><svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor" opacity=".22"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg><p>Ketik judul drama untuk mencari</p><p class="empty-sub">Cari dari 10 platform sekaligus</p><div class="trending-search"><b>Lagi dicari</b><div>'+chips.map(q=>`<button class="trend-chip" onclick="quickSearch('${jsStr(q)}')">${esc(q)}</button>`).join('')+'</div></div></div>'}
function quickSearch(q){openSearch();setTimeout(()=>{const inp=$('#sInp');if(inp){inp.value=q;doSearch()}},80)}
function getRecentSearches(){try{return JSON.parse(localStorage.getItem('dk_recent_searches')||'[]')}catch(e){return[]}}
function saveRecentSearch(q){q=String(q||'').trim();if(q.length<2)return;let a=getRecentSearches().filter(x=>x.toLowerCase()!==q.toLowerCase());a.unshift(q);if(a.length>8)a=a.slice(0,8);try{localStorage.setItem('dk_recent_searches',JSON.stringify(a))}catch(e){}}
function clearRecentSearches(){try{localStorage.removeItem('dk_recent_searches')}catch(e){};renderSearchTools()}
function useSearch(q){$('#sInp').value=q;doSearch()}
function renderSearchTools(){const el=$('#sTools');if(!el)return;if(lastSearchResults.length&&lastSearchQuery){const counts={all:lastSearchResults.length};lastSearchResults.forEach(d=>{const p=d._p||platCache[d.drama_id]||P;counts[p]=(counts[p]||0)+1});const chips=Object.keys(counts).sort((a,b)=>a==='all'?-1:b==='all'?1:platformLabel(a).localeCompare(platformLabel(b))).map(p=>`<button class="chip ${searchFilter===p?'on':''}" onclick="setSearchFilter('${jsStr(p)}')">${p==='all'?'Semua':esc(platformLabel(p))}<span>${counts[p]}</span></button>`).join('');el.innerHTML=`<div class="search-meta"><span>Filter platform</span><span>${lastSearchResults.length} hasil</span></div><div class="search-chips">${chips}</div>`;return}const rec=getRecentSearches();if(!rec.length){el.innerHTML='';return}el.innerHTML=`<div class="search-meta"><span>Terakhir dicari</span><button class="chip danger" style="min-height:26px;padding:5px 9px" onclick="clearRecentSearches()">Hapus</button></div><div class="search-chips">${rec.map(q=>`<button class="chip" onclick="useSearch('${jsStr(q)}')"><span class="live-dot"></span>${esc(q)}</button>`).join('')}</div>`}
function setSearchFilter(p){searchFilter=p;renderSearchResults(lastSearchResults,lastSearchQuery)}
function renderSearchResults(items,q){const box=$('#sRes');renderSearchTools();const list=searchFilter==='all'?items:items.filter(d=>(d._p||platCache[d.drama_id]||P)===searchFilter);const head=`<div class="result-head"><span><strong>${list.length}</strong> judul ${searchFilter==='all'?'ditemukan':'di '+esc(platformLabel(searchFilter))}</span><span>“${esc(q)}”</span></div>`;if(list.length)box.innerHTML=head+`<div class="grid">${list.map(cardHtml).join('')}</div>`;else box.innerHTML=head+emptyHtml('Tidak ada hasil di filter ini','Coba pilih platform lain atau kata kunci lain')}

function openSearch(){$('#sOv').classList.add('on');renderSearchTools();setTimeout(()=>{$('#sInp').focus();$('#sInp').select()},150)}
function closeSearch(){$('#sOv').classList.remove('on');$('#sInp').value='';lastSearchResults=[];lastSearchQuery='';searchFilter='all';$('#sRes').innerHTML=searchEmptyHtml();renderSearchTools()}
function debSearch(){clearTimeout(sto);sto=setTimeout(doSearch,350)}

function dedupeSearchResults(items){
  const byId=new Set();
  const byTitlePlat=new Set();
  const byTitle=new Map(); // titleKey -> best item
  const out=[];
  (items||[]).forEach(d=>{
    if(!d)return;
    const id=String(d.drama_id||'').trim();
    const plat=d._p||platCache[id]||'';
    const titleKey=normalizeTitleKey(d.drama_name||d.title||'');
    if(!id&&!titleKey)return;
    const idKey=plat+'|'+id;
    if(id&&byId.has(idKey))return;
    if(id)byId.add(idKey);
    const tpKey=plat+'|'+titleKey;
    if(titleKey&&byTitlePlat.has(tpKey))return;
    if(titleKey)byTitlePlat.add(tpKey);
    // Cross-platform near-duplicate: keep the one with more metadata
    if(titleKey&&byTitle.has(titleKey)){
      const prev=byTitle.get(titleKey);
      const score=(x)=>(x.thumb_url?2:0)+(x.episode_count?1:0)+(extractRealRating(x)?2:0)+(x.description?1:0)+(x.watch_value?1:0);
      if(score(d)>score(prev)){
        const idx=out.indexOf(prev);
        if(idx>=0)out[idx]=d;
        byTitle.set(titleKey,d);
      }
      // still keep other platforms if ids differ? For cleaner UX hide cross-platform dups.
      return;
    }
    if(titleKey)byTitle.set(titleKey,d);
    out.push(d);
  });
  return out;
}
function rankSearchResults(items,q){
  const query=normalizeTitleKey(q);
  const tokens=query.split(' ').filter(Boolean);
  return (items||[]).map(d=>{
    const title=normalizeTitleKey(d.drama_name||d.title||'');
    let s=0;
    if(title===query)s+=100;
    else if(title.startsWith(query))s+=70;
    else if(title.includes(query))s+=45;
    tokens.forEach(t=>{if(title.includes(t))s+=8});
    if(extractRealRating(d))s+=6;
    if(d.thumb_url)s+=3;
    if(parseInt(d.episode_count)>0)s+=2;
    if(d.watch_value)s+=1;
    // slight platform priority for completeness
    const pref={moviebox:3,drakor:3,melolo:2,dramabox:2,dramanova:1};
    s+=(pref[d._p]||0);
    return {d,s};
  }).sort((a,b)=>b.s-a.s||String(a.d.drama_name||'').localeCompare(String(b.d.drama_name||''))).map(x=>x.d);
}

async function doSearch(){
  const q=$('#sInp').value.trim(),box=$('#sRes');
  if(!q){lastSearchResults=[];lastSearchQuery='';searchFilter='all';renderSearchTools();box.innerHTML=searchEmptyHtml();return}
  const seq=++searchSeq;lastSearchQuery=q;searchFilter='all';saveRecentSearch(q);renderSearchTools();
  box.innerHTML=`<div class="grid">${skelHtml(6,1)}</div>`;
  try{const eq=encodeURIComponent(q);
    const[r1,r2,r3,r4,r5,r6,r7,r8,r9,r10]=await Promise.allSettled([
      cachedJson(API.melolo+`/search?q=${eq}&page=1&lang=id`,120000),
      cachedJson(API.freereels+`/search?q=${eq}&page=1&lang=id`,120000),
      cachedJson(API.flickreels+`/search?q=${eq}`,120000),
      cachedJson(API.dramanova+`/search?q=${eq}&page=1&size=10`,120000),
      cachedJson(API.reelshort+`/search?q=${eq}&page=1&limit=10`,120000),
      cachedJson(API.netshort+`/search?query=${eq}&page=1`,120000),
      cachedJson(API.dramabox+`/search?q=${eq}&page=1&lang=in`,120000),
      cachedJson(API.goodshort+`/search?q=${eq}&page=1`,120000),
      cachedJson(API.moviebox+`/search?q=${eq}&page=1&perPage=10`,120000),
      cachedJson(API.drakor+`/search?q=${eq}&page=1&limit=30&type=1&order=1`,120000),
    ]);
    if(seq!==searchSeq)return;
    const tag=(r,p)=>{if(!platformEnabled(p))return[];const items=r.status==='fulfilled'?flat(r.value.data):[];items.forEach(d=>{if(d.drama_id){platCache[d.drama_id]=p;d._p=p}});return items};
    const raw=[...tag(r1,'melolo'),...tag(r2,'freereels'),...tag(r3,'flickreels'),...tag(r4,'dramanova'),...tag(r5,'reelshort'),...tag(r6,'netshort'),...tag(r7,'dramabox'),...tag(r8,'goodshort'),...tag(r9,'moviebox'),...tag(r10,'drakor')];
    const a=rankSearchResults(dedupeSearchResults(raw),q);
    const failed=([r1,r2,r3,r4,r5,r6,r7,r8,r9,r10].filter(r=>r.status==='rejected').length);
    lastSearchResults=a;renderSearchResults(a,q);
    if(!a.length)box.innerHTML=emptyHtml('Tidak ditemukan untuk "'+q+'"',failed?'Beberapa platform gagal dimuat. Coba lagi.':'Coba kata kunci lain');
    else if(failed)toast(failed+' platform gagal dimuat');
  }catch(e){ErrorLog.capture('search',String(e?.message||e),{q:lastSearchQuery});if(seq===searchSeq)box.innerHTML=errHtml(e?.message||'Gagal mencari di beberapa platform')}
}

window.addEventListener('scroll',()=>{
  // Infinite scroll
  if(curTab!=='home'&&curTab!=='history'&&curTab!=='fav'){const trg=$('#trg-'+curTab);if(trg&&trg.getBoundingClientRect().top<window.innerHeight+300)loadTab(curTab)}
  // Scroll to top button
  const btn=$('#scrollTop');if(btn){if(window.scrollY>400)btn.classList.add('show');else btn.classList.remove('show')}
});
