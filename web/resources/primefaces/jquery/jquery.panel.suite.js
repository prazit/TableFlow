(function(a){Appanel.version+=" JPSR85";a.fn.inout=function(n,m){if(this.length==0){return this}var h="function",q="inout:topin",d="inout:topout",o="inout:bottomin",k="inout:bottomout",p=this.first(),c=p.get(0),f=p.is("body")?a(window):p,l=false,g=n instanceof jQuery,b=0,i=0,e;if(g){e=n;if(m!=undefined){i=m}}else{if(typeof n==="string"){if(n==="remove"){l=true}else{if(n==="update"){j();return}}e=m}else{b=n;e=m}}if(c.inout==undefined){c.inout={over:[],inside:[],under:[],dynamic:[],container:p,scroller:f,top:f.scrollTop(),bottom:f.scrollTop()+f.innerHeight(),id:0,target:[],element:function(t,u){var v=this.container,s=typeof(u)==h?function(){return t.offset().top-u.call(this)-v.offset().top}:function(){return t.offset().top-u-v.offset().top},r=typeof(u)==h?function(){return t.offset().top+t.height()+u.call(this)-v.offset().top}:function(){return t.offset().top+t.height()+u-v.offset().top},w=this.reg(t);this.point(w,r,t,q,d,undefined,undefined);this.point(w,s,t,undefined,undefined,o,k)},reg:function(r){var s=r.get(0);if(s.inoutID===undefined){s.inoutID=this.id;this.target[this.id]=r;this.id++}return s.inoutID},point:function(w,s,u,B,t,x,v){var A={id:w,target:u,topin:B,topout:t,bottomin:x,bottomout:v,toString:this.string},z=typeof(s)===h,y,r;if(z){this.dynamic.push(A);A.getLevel=s;s=s.call(A)}if(s<0){s=0}y=this.pos(s);A.level=s;A.pos=y;if(y===0){this.over.push(A)}else{if(y===1){this.inside.push(A)}else{this.under.push(A)}}r=this.event(20+y,A);if(r!==undefined){this.trigger(r)}},update:function(){var x=c.inout,v=[],y=x.top,r=x.bottom,w,z,u,t,A,s;x.top=f.scrollTop();x.bottom=x.top+f.innerHeight();if(x.top<y){x.pair(v,0,x.over,1,x.inside)}else{x.pair(v,1,x.inside,0,x.over)}if(x.bottom<r){x.pair(v,1,x.inside,2,x.under)}else{x.pair(v,2,x.under,1,x.inside)}u=x.dynamic.length;for(w=0;w<u;w++){z=x.dynamic[w];A=z.pos*10;t=z.getLevel.call(z);if(t<0){t=0}z.pos=x.pos(t);s=x.event(A+z.pos,z);if(s!==undefined){while(s.length>0){v.push(s.shift())}}}x.trigger(v)},pos:function(r){if(r<this.top){return 0}else{if(r<this.bottom){return 1}else{return 2}}},event:function(s,r){switch(s){case 1:return[[r,r.topin]];case 2:return[[r,r.topin],[r,r.bottomout]];case 10:return[[r,r.topout]];case 12:return[[r,r.bottomout]];case 20:return[[r,r.bottomin],[r,r.topout]];case 21:return[[r,r.bottomin]];default:return undefined}},pair:function(t,A,x,u,y){var v,w,z,r,s=x.length;A*=10;for(v=0;v<s;v++){z=x[v];w=this.pos(z.level);if(w===u){r=this.event(A+w,z);if(r!==undefined){s--;x.splice(v--,1);y.push(z);while(r.length>0){t.push(r.shift())}}z.pos=w}}},trigger:function(r){var u,t="";for(var s in r){u=r[s];if(u[1]!==undefined){u[0].target.triggerHandler(u[1])}}},string:function(){return"{id:"+this.id+",pos:"+this.pos+", target:"+this.target+"}"},clear:function(r){var u=[this.over,this.inside,this.under,this.dynamic],w=this.reg(r),v,t;for(var s in u){v=u[s];for(t=0;t<v.length;t++){if(v[t].id===w){v.splice(t,1);t--}}}}};f.on("scroll",j);f.on("content:updated",j);Appanel.router.on("resize",j)}function j(){clearTimeout(c.inout.updateID);c.inout.updateID=setTimeout(c.inout.update,Appanel.milli("scroll"))}e.each(function(s,t){var r=a(t);if(g){c.inout.element(r,i)}else{if(l){c.inout.clear(r)}else{c.inout.point(c.inout.reg(r),b,r,q,d,o,k)}}});return this};a.fn.entry=function(c){var d=c;c=a.extend({enter:true,submit:false,fit:false},c===undefined?{}:c);function f(g){var j=g.length-1,h,k;if(c.enter){for(h=0;h<j;h++){k=g.get(h);k.nextInput=g.get(h+1);a(k).on("keydown",function(i){if(i.keyCode===13){i.preventDefault();a(this.nextInput).focus();i.stopPropagation()}})}}if(c.submit){a(g[j]).on("keydown",function(i){if(i.keyCode==13){i.preventDefault();a(this).closest("form").submit()}})}}function b(i){if(c.fit){var h=a(i),j=h.parent(),g=j.width()+j.offset().left;h.width(g-h.offset().left)}}function e(){var j=this,i=a(j),h=i.attr("match"),g=this.entryContext.option;j.entryContext=d;j.acceptedText=i.val();b(j);if(typeof(h)==="string"&&h.length>0){i.on("keydown",function(k){var l=a(this).val();if(l.match(g.match)>=0){j.acceptedText=l}else{i.val(j.acceptedText)}})}}return this.each(function(j,k){var h=a(k),g=h.find("input").not("[readonly]");context={all:g,checkbox:undefined,dropdown:undefined,file:undefined,image:undefined,option:c,radio:undefined,text:g.filter("[type=text]")};f(g);k.entryContext=context;d=context;context.text.each(e)})};a.fn.toTag=function(b){if(this.length==0){return this}var g=this.first(),c=g.prop("tagName"),f=c.length+1,e=g.get(0).outerHTML.substr(f),d;e="<"+b+e.substr(0,e.length-f)+b+">";return g.replaceWith(e)};a.sweepPlayer={d:0,dummy:function(b){if(this.d===0){this.d=Appanel.router.append('<i class="hidden" id="dummy"></i>').find("#dummy")}return this.d.append('<i id="'+b+'"></i>').find("#"+b)},lid:0,id:function(){return"sweep"+(++this.lid)},s:[],start:function(b){b.active=true},end:function(b){this.s=a.extend(this.s,b.s);b.active=false;b.s=[]},sweeper:function(d,c){var b=d.label.indexOf(c);if(b<0){return undefined}else{return d.sweepers[b]}},waiting:function(b,d){var e=b.length,k=[],h=[],j=this.active?this.ss:this.s,g,f,c;for(g=0;g<e;g++){c=b[g];if(typeof c==="string"){c=this.sweeper(d,c);if(c==undefined){continue}}c.link=k;h.push(c);f=c.$.get(0).sweepID;if(j.indexOf(f)<0){j.push(f);j[f]=[c]}else{j[f].push(c)}if(k.indexOf(f)<0){k.push(f)}}return k},playing:function(m){var h=[],l=a.extend([],m),f,b,e,k,c,d,j;for(f=0;f<l.length;f++){d=l[f];j=this.s.indexOf(d);if(j>=0){for(e=0;e<=j;e++){k=this.s.shift(j);if(k==d){break}else{this.s.push(k)}}b=this.s[d];this.s[d]=undefined;while(b.length>0){c=b.shift();h.push(c);if(c.link!=undefined){while(c.link.length>0){var g=c.link.shift();if(l.indexOf(g)<0){l.push(g)}}c.link=undefined}}}}while(h.length>0){this.play(h.shift(),1)}},playAll:function(e,c){var d=e.length,f,b;this.start(c);for(b=0;b<d;b++){if(typeof e[b]==="string"){f=this.sweeper(c,e[b]);if(f!=undefined){this.play(f)}}else{if(e[b].on==undefined){this.play(e[b])}}}this.end(c)},play:function(s,m){if(m==undefined){this.playing([s.$.get(0).sweepID])}if(s.context.pause){s.context.play.push(s);return}var r=s.find==undefined?s.$:s.$.find(s.find),g=r.get(0),x=s.context.speed;if(typeof s.runonce==="function"){try{s.runonce.call(s,g,s.runarg)}catch(A){Appanel.th("error",A)}finally{s.runone=s.runonce;s.runonce=false}}if(typeof s.run==="function"){try{s.run.call(s,g,s.runarg)}catch(A){Appanel.th("error",A)}}var l=r.attr("class"),u=l==undefined?"":l,h=u.split(" "),w=r.attr("style"),y=w==undefined?"":w,n=y+"",C=s.play!=undefined,z=s.csstrans!=undefined&&s.csstrans===true,v=s.sweep!=undefined,i=s.defer!=undefined;if(s.remove!=undefined){var q=s.remove.split(" "),o=0,f,t;while(o<q.length){f=q[o];t=h.indexOf(f);if(t>=0){h.splice(t,1)}o++}}if(v||i){if(v){var c=this.waiting(s.sweep,s.context),k=this.animationEnd,p=Appanel.milli(z||s.loop==undefined?s.duration:s.duration*s.loop)*x;setTimeout(function(){k.call(g,c)},p)}if(i){var j=s.defer.length,B;for(B=0;B<j;B++){this.defer(g,s.defer[B],s.context)}}}if(s.add!=undefined){var q=s.add.split(" "),f;for(var o in q){f=q[o];if(h.indexOf(f)<0){h.push(f)}}if(s.add.search(s.hidden)>=0){r.attr("class",s.hidden)}}n=n.replaceAll(/-webkit-animation-duration:[^;]*;|animation-duration:[^;]*;|-webkit-animation-iteration-count:[^;]*;|animation-iteration-count:[^;]*;|-webkit-animation-timing-function:[^;]*;|animation-timing-function:[^;]*;|-webkit-transition-duration:[^;]*;|transition-duration:[^;]*;|-webkit-transition-timing-function:[^;]*;|transition-timing-function:[^;]*;/,"");var b=s.duration*x;if(C){n+="-webkit-animation-duration:"+b+"s;animation-duration:"+b+"s;-webkit-animation-iteration-count:"+s.loop+";animation-iteration-count:"+s.loop+";";if(s.timing!=undefined){n+="-webkit-animation-timing-function:"+s.timing+";animation-timing-function:"+s.timing+";"}var q=s.play.split(" "),f;for(var o in q){f=q[o];if(h.indexOf(f)<0){h.push(f)}}}if(z){n+="-webkit-transition-duration:"+b+"s;transition-duration:"+b+"s;";if(s.timing!=undefined){n+="-webkit-transition-timing-function:"+s.timing+";transition-timing-function:"+s.timing+";"}}if(n!==y){r.attr("style",n)}h=h.join(" ");if(h!==u){r.attr("class",h)}if(typeof s.css==="object"){r.css(s.css)}},animationEnd:function(c){var b=a.sweepPlayer;b.playing(c)},defer:function(c,e,b){var d=this,f=Appanel.milli(e.duration)*b.speed;setTimeout(function(g){d.playAll(e.sweep,b)},f)},pause:function(b){b.pause=true},resume:function(b){if(b.pause){var c=b.play;b.play=[];b.pause=false;this.playAll(c,b)}},handle:function(f,I,p,b,v,q){q.context=f;if(q.label!=undefined){var n=q.label;if(f.label.indexOf(n)<0){f.label.push(n);f.sweepers.push(q)}}if(q.hidden!=undefined){I=q.hidden}var G,A;if(q.duration!=undefined){G=q.duration}else{G=b}if(q.timing!=undefined){A=q.timing}else{A=v}if(q.selector!=undefined){p=(q.selector instanceof jQuery)?q.selector:a(q.selector);if(!p.length){p=this.dummy(this.id())}}q.$=p;if(p.get(0).sweepID==undefined){p.get(0).sweepID=this.id()}if(q.handled==undefined){var c=q.sweep!=undefined,w=q.defer!=undefined,x=q.play!=undefined,z=q.on!=undefined,D=q.csstrans!=undefined&&q.csstrans===true,l;if(c){q.sweep=a.extend([],q.sweep);for(var u=0;u<q.sweep.length;u++){l=q.sweep[u];if(typeof l!=="string"){q.sweep[u]=a.extend({},l)}}}if(w){q.defer=a.extend([],q.defer);for(var F=0;F<q.defer.length;F++){q.defer[F]=a.extend({},q.defer[F]);if(q.defer[F].sweep!=undefined){q.defer[F].sweep=a.extend([],q.defer[F].sweep);for(var u=0;u<q.defer[F].sweep.length;u++){l=q.defer[F].sweep[u];if(typeof l!=="string"){q.defer[F].sweep[u]=a.extend({},l)}}}}}var C=c?q.sweep:[],h=C.length;q.handled=true;if(q.add!=undefined||x||D||q.remove!=undefined){q.timing=A;q.duration=G==undefined||G<0?0.001:G;if(q.loop==undefined||q.loop<=0){q.loop=1}}else{q.duration=0.001}if(x){if(q.remove==undefined){q.remove=I}else{if(q.remove.search(I)<0){q.remove+=" "+I}}var H=q.play.replace(I,"");if(c){var k=true,g=null,t,u;for(t=0;t<h;t++){u=C[t];if(typeof u!=="string"&&u.selector==undefined){g=u;if(u.remove!=undefined&&u.on==undefined&&u.selector==undefined){k=false;C[t].remove=H+" "+C[t].remove.replace(I,"");break}}}if(k){if(g==null){C[h]={remove:H};g=C[h];h++}else{g.remove=H}}}else{C=[{remove:H}];c=true;h=1;q.sweep=C}}if(D){if(q.remove==undefined){q.remove=I}else{if(q.remove.search(I)<0){q.remove+=" "+I}}var H=undefined;if(c){var B=true,t;for(t=0;t<h;t++){if(typeof C[t]!=="string"&&C[t].selector==undefined){B=false;break}}if(B){C.unshift({run:H});h++}}else{C=[{run:H}];c=true;h=1;q.sweep=C}}var o=[];if(c){for(var y=0;y<h;y++){if(typeof C[y]==="object"&&this.handle(f,I,p,b,v,C[y])){o.push(C[y]);C.splice(y--,1);h--}}if(C.length==0){q.sweep=undefined}}if(w){var j=q.defer.length,E,e;for(E=0;E<j;E++){e=q.defer[E];h=e.sweep.length;if(e.duration==undefined){e.duration=0.001}for(var y=0;y<h;y++){if(typeof e.sweep[y]==="object"&&this.handle(f,I,p,b,v,e.sweep[y])){o.push(e.sweep);e.sweep.splice(y--,1);h--}}if(e.sweep.length==0){q.defer.splice(E--,1);j--}}}if(o.length>0){q.actions=o}if(z){var m=this,r=p.get(0).sweepID;p.on(q.on,function(i,d){q.runarg=d;m.play(q)});return true}}return false}};a.fn.sweep=function(l,e){if(this.length==0){return this}var j=a.sweepPlayer,m=this.first(),f=m.get(0),i=typeof(l)==="string",d=i?f.sweepContext:{label:[],sweepers:[],s:[],pause:false,play:[],speed:(l.speedx!=undefined?1/l.speedx:(l.speed!=undefined?l.speed:1))};if(i){if(l==="resume"){for(var h in d){j.resume(d[h])}}else{if(l==="pause"){for(var h in d){j.pause(d[h])}}else{if(l==="speedx"){if(e==undefined){return 1/d[0].speed}else{var b=1/e;for(var h in d){d[h].speed=b}}}else{if(l==="speed"){if(e==undefined){return d[0].speed}else{if(e>0){for(var h in d){d[h].speed=e}}}}}}}}else{l=a.extend({duration:Appanel.duration.animate,on:"click touchstart"},l);d.sweep=l;if(f.sweepContext==undefined){f.sweepContext=[d]}else{f.sweepContext.push(d)}if(l.inout!=undefined){var g=l.inout,k=g.scope==undefined?m:a(g.scope);a(g.scroller!=undefined?g.scroller:"body").inout(k.on("inout:topin inout:bottomin",function(){j.resume(d)}).on("inout:topout inout:bottomout",function(){j.pause(d)}),g.margin!=undefined?g.margin:function(){return this.target.height()/3*-2})}j.handle(d,"hidden",m,l.duration,l.timing,l);m.triggerHandler("sweep:ready")}return this};a.fn.glass=function(e){var f={elementLayer:false,container:Appanel.router,layer:5,color:"black",opacity:0.6,duration:Appanel.duration.animate,scrollLock:true,onclick:function(){a.glass.scroll()}},g="glass:show",b="glass:hide";if(this.length==0){return this}if(a.glass==undefined){a.glass={stack:[],scrollTop:0,scrollX:0,scrollY:0,lockScroll:function(){this.scrollX=window.scrollX;this.scrollY=window.scrollY;if(this.scrollLock==undefined){this.scrollLock=function(h){var i=a.glass;if(h.type!="scroll"){i.scrollX=window.scrollX;i.scrollY=window.scrollY}else{if(i.scrollX!=window.scrollX||i.scrollY!=window.scrollY){window.scrollTo(i.scrollX,i.scrollY)}}};a(window).bind("scroll",this.scrollLock)}},unlockScroll:function(){if(this.scrollLock!=undefined){a(window).unbind("scroll",this.scrollLock);this.scrollLock=undefined}},scroll:function(i){var h=a.glass;if(h.scrollTop!=0&&h.scrollElement!=undefined){h.scrollElement.animate({scrollTop:h.scrollTop},Appanel.milli("animate"))}if(i!=undefined){i.stopPropagation()}},clicking:false,show:function(j){var i={opacity:0,backgroundColor:"transparent"},m=this.glass===undefined?this.glass=Appanel.router.append('<div id="glass" class="hidden on-top-left fit-width fit-height">&nbsp;</div>').find("div#glass").sweep({on:g,sweep:[{remove:"hidden",css:a.extend({},i),duration:0.01,sweep:[{csstrans:true,css:a.extend({},i),duration:1,run:function(){var n=a.glass.context;this.css.backgroundColor=n.color;this.css.opacity=n.opacity;this.duration=n.duration}}]},{on:b,sweep:[{css:{opacity:j.opacity},duration:0.01,run:function(){this.css.opacity=a.glass.context.opacity},sweep:[{csstrans:true,css:i,duration:1,run:function(){this.duration=a.glass.context.duration},sweep:[{add:"hidden",duration:0.01,run:function(){a.glass.hidden()}}]}]}]}]}):this.glass,k="click touchstart",h=a(j.hole);if(this.context!==undefined&&j.hole.glassID!==this.context.hole.glassID){this.deglass(this.context,false);j.alreadyShow=true}else{j.alreadyShow=false}this.context=j;if(typeof j.container==="string"){j.container=a(j.container)}j.container.append(m);if(typeof(j.onclick)==="function"){m.one(k,function(){j.onclick.call(j)})}this.showing=true;if(j.scrollLock){this.lockScroll()}else{this.unlockScroll()}if(this.bindedOn){this.bindedOn=false;this.bindedFn()}this.scrollElement=j.container;m.addClass("layer-"+j.layer);if(j.elementLayer!==false){h.addClass("layer-"+j.elementLayer)}var l=this;setTimeout(function(){l.showing=false},Appanel.milli(j.duration));m.triggerHandler(g)},englass:function(i){var j=i.hole,h=j.glassID;if(h!==undefined){this.deglass(i,false)}j.glassID=this.stack.length;this.stack.push(j);this.show(i)},deglass:function(j,i){var l=j.hole,h=l.glassID;if(h==undefined){return}l.glassID=undefined;this.glass.removeClass("layer-"+j.layer);if(j.elementLayer!==false){a(l).removeClass("layer-"+j.elementLayer)}this.glass.off("click touchstart");this.unused(h);if(this.stack.length>0){var k=this.stack.pop();k.glassID=undefined;this.englass(k.glassOptions)}else{if(i!==false){this.hide(j)}}},unused:function(i){var h=this.stack.length-i;while(h-->0){this.stack.pop()}},hidden:function(){this.hiding=false;if(!this.glass.parent().is(Appanel.router)){Appanel.router.append(this.glass)}$this.unlockScroll()},hiding:false,hide:function(j){if(this.glass==undefined||this.hiding){return}this.hiding=true;this.scrollTop=0;this.scrollElement=undefined;this.glass.triggerHandler(b);for(var h in this.stack){this.stack[h].glassID=undefined}this.stack=[]},showing:false}}var c,d=this.first().get(0);if(typeof(e)=="string"){c=d.glassOptions;if(c==undefined){c=a.extend({},f);c.hole=d;d.glassOptions=c}if(e=="refresh"){a.glass.englass(c)}else{if(e=="remove"){a.glass.deglass(c)}else{if(e=="removeall"){a.glass.hide();a.glass.unused(0)}}}}else{if(e==undefined){c=d.glassOptions;if(c==undefined){c=a.extend({},f)}}else{c=a.extend({},f,e)}c.hole=d;d.glassOptions=c;a.glass.englass(c)}return this};a.fn.live=function(c){if(this.length==0){return this}if(c===undefined){c={}}var b=a.extend({content:"div.live",iframe:"div.iframe",image:"div.image",margin:0,trigger:"live:content",scroller:false},c);function g(m,l,j,i,k){m.find(j).each(function(o,p){var n=a(p);if(l!==false){n.one("inout:bottomin",function(){l.inout("remove",n);if(k==undefined){i.call(n.get(0))}else{i.call(n.get(0),k)}});l.inout(n,b.margin)}else{if(k==undefined){i.call(n.get(0))}else{i.call(n.get(0),k)}}})}function f(){var j=a(this),i=this.liveOptions,k;if(i!==undefined){k=i.scroller;g(j,k,i.content,e,i);g(j,k,i.iframe,d);g(j,k,i.image,h)}}function e(j){var i=a(this),k=i.attr("src");if(typeof(k)==="string"&&this.isLiveContent===undefined){this.isLiveContent=true;i.addClass("hidden").after(Appanel.loading.map({text:Appanel.language.loading}));i.load(k,function(l){i.trigger(j.trigger).fadeIn().removeClass("hidden").next().remove();f.call(this)})}else{f.call(this)}}function d(){var i=a(this),l=i.attr("src");if(typeof(l)==="string"&&this.isLiveIframe===undefined){this.isLiveIframe=true;var j="onload",k=i.attr(j);i.attr(j,"$(this).removeAttr('"+j+"').fadeIn().prev().remove();"+(k?k:"")).addClass("hidden").before(Appanel.loading.map({text:Appanel.language.loading}));i.toTag("iframe")}else{}}function h(){var i=a(this),j=i.attr("src");if(typeof(j)==="string"&&this.isLiveIframe===undefined){this.isLiveIframe=true;i.toTag("img")}else{}}return this.each(function(j,k){k.liveOptions=b;e.call(k,b)})};a.fn.panel=function(d){var c={color:"transparent"},g={position:"right",width:false,height:false,duration:0.4,pull:false,pullthis:"body",container:"body",glass:c,sweep:false,live:false,entry:false,trigger:false,command:""},j=g,f,b="panel:preshow",i="panel:prehide";if(typeof(d)=="string"){f=d}else{j=a.extend({},g,d);f=j.command}function h(k){return k?(typeof(k)=="string"&&k.search("[autoempxvwt%]")>=0?k:k+"px"):"100%"}function e(k){var l=k.search("[0-9]");return l>=0?k.substring(0,l)+"-"+k.substring(l):k}j.width=h(j.width);j.height=h(j.height);return this.first().each(function(x,B){var k=a(B);if(B.isPanel==undefined){B.isPanel=true;var q={position:j.container=="body"?"fixed":"absolute",width:j.width,height:j.height},u="panel:show",l="panel:hide",m="sweep:hide",t=k.css("z-index"),w=a(j.container),A,n,D,r;if(t==undefined||t=="auto"||t==0){q.zIndex=78888}if(j.sweep){k.addClass("hidden");D=a.extend({},j.sweep.onShow);r=a.extend({},j.sweep.onHide);q.maxWidth="100%";q.maxHeight="100%";if(j.position=="left"){q.left=0;q.top=0}else{if(j.position=="right"){q.right=0;q.top=0}else{if(j.position=="top"){if(j.left==undefined){q.left="50%";q.marginLeft="calc( -1 * ("+j.width+" / 2) )"}else{q.left=j.left+"px"}q.top=0}else{if(j.position=="bottom"){if(j.left==undefined){q.left="50%";q.marginLeft="calc( -1 * ("+j.width+" / 2) )"}else{q.left=j.left+"px"}q.bottom=0}else{q.top="35%";q.left="50%";if(j.width==="100%"){q.width="auto"}if(j.height==="100%"){q.height="auto"}q.transform="translate(-50%,-50%)"}}}}}else{var y=e(j.width),s=e(j.height);if(j.position=="left"){n={left:y};A={left:0};q.maxWidth="100%";q.left=n.left;q.top=0}else{if(j.position=="right"){n={right:y};A={right:0};q.maxWidth="100%";q.right=n.right;q.top=0}else{if(j.position=="top"){n={top:s};A={top:"0"};if(j.left==undefined){q.left="50%";q.marginLeft="calc( -1 * ("+j.width+" / 2) )"}else{q.left=j.left+"px"}q.top=n.top}else{if(j.position=="bottom"){n={bottom:s};A={bottom:"0"};if(j.left==undefined){q.left="50%";q.marginLeft="calc( -1 * ("+j.width+" / 2) )"}else{q.left=j.left+"px"}q.bottom=n.bottom}else{n={top:"150%",opacity:0};A={top:"35%",opacity:1};q.left="50%";if(j.width==="100%"){q.width="auto"}if(j.height==="100%"){q.height="auto"}q.top="150%";q.opacity=0;q.transform="translate(-50%,-50%)"}}}}D={sweep:[{csstrans:true,css:A}]};r={sweep:[{csstrans:true,css:n}]}}if(!w.is(k.parent())){w.append(k)}k.css(q);if(j.pull&&j.position.search("left|right")==0){var E,p,C,o,v=j.width,z=j.pull!="soft";if(z){if(j.position=="left"){E={marginLeft:v,marginRight:"-"+v};p={marginLeft:0,marginRight:0}}else{E={marginRight:v,marginLeft:"-"+v};p={marginRight:0,marginLeft:0}}}else{if(j.position=="left"){E={marginLeft:v};p={marginLeft:0}}else{E={marginRight:v};p={marginRight:0}}}C={selector:j.pullthis,duration:j.duration,csstrans:true,css:E};o=a.extend({},C);o.css=p;if(D.sweep==undefined){D.sweep=[C]}else{D.sweep.unshift(C)}if(r.sweep==undefined){r.sweep=[o]}else{r.sweep.unshift(o)}}D.on=u;r.on=l;if(D.duration==undefined){D.duration=j.duration}if(r.duration==undefined){r.duration=j.duration}if(j.debug){D.debug=true}k.sweep(D).sweep(r);if(j.entry){k.entry(j.entry)}if(j.live){k.one(u,function(){k.live(j.live)})}if(j.trigger!=undefined){if(j.trigger.show!=undefined){var D=j.trigger.show;a(D.selector).on(D.on==undefined?"click touchstart":D.on,function(){k.triggerHandler(b);k.triggerHandler(u)})}if(j.trigger.hide!=undefined){var r=j.trigger.hide;a(r.selector).on(r.on==undefined?"click touchstart":r.on,function(){k.triggerHandler(i);k.triggerHandler(l)})}}if(j.glass){var F=typeof(j.glass)=="object"?a.extend({},c,j.glass):a.extend({},c);if(F.duration==undefined){F.duration=j.duration}if(F.container==undefined){if(j.pullthis==undefined){F.container=j.container}else{F.container=j.pullthis}}k.on(u,function(){k.glass(F)});k.on(l+" "+m,function(){k.glass("remove")})}if(f=="show"){k.triggerHandler(b);k.triggerHandler(u)}return}if(f!=""){k.triggerHandler("panel:pre"+f);k.triggerHandler("panel:"+f)}})};a.fn.drilldown=function(c){var s="drilldown:nexthide",g="drilldown:nextshow",u="drilldown:backhide",j="drilldown:backshow",p="drilldown:prehide",b="drilldown:preshow",q="click touchstart",l={hidden:"hidden",sub:".sub",next:".next",back:".back",home:".home",live:{trigger:"drilldown:live"},sweep:{duration:0.4,onNextIn:{sweep:[{duration:0.02,css:{marginLeft:"100%"},remove:"hidden",sweep:[{duration:0.4,csstrans:true,css:{marginLeft:"0%"}}]}]},onNextOut:{sweep:[{duration:0.02,sweep:[{csstrans:true,css:{marginLeft:"-100%"},sweep:[{add:"hidden"}]}]}]},onBackIn:{sweep:[{duration:0.02,css:{marginLeft:"-100%"},remove:"hidden",sweep:[{duration:0.4,csstrans:true,css:{marginLeft:"0%"}}]}]},onBackOut:{sweep:[{duration:0.02,sweep:[{csstrans:true,css:{marginLeft:"100%"},sweep:[{add:"hidden"}]}]}]}}},e=this.first(),i,r,n,m;if(e.length==0){return this}if(typeof(c)=="string"){e.triggerHandler("drilldown:"+c);return this}i=a.extend({},l,c);m=[];if(i.sweep===false){i.sweep=l.sweep}else{i.live=a.extend({},l.live,i.live);i.sweep=a.extend({},i.sweep);i.sweep.onNextIn=a.extend({},i.sweep.onNextIn);i.sweep.onNextOut=a.extend({},i.sweep.onNextOut);i.sweep.onBackIn=a.extend({},i.sweep.onBackIn);i.sweep.onBackOut=a.extend({},i.sweep.onBackOut)}function t(z,y){if(z.duration==undefined){z.duration=i.sweep.duration!=undefined?i.sweep.duration:0.358}z.on=y;z.hidden=i.hidden;if(i.debug!=undefined){z.debug=true}}t(i.sweep.onBackOut,u);t(i.sweep.onBackIn,j);t(i.sweep.onNextOut,s);t(i.sweep.onNextIn,g);var w=a.extend({},i.sweep.onBackOut);if(w.sweep==undefined){w.sweep=[]}w.sweep.push(i.sweep.onBackIn);w.sweep.push(i.sweep.onNextOut);w.sweep.push(i.sweep.onNextIn);if(m.length==0){r=e;m.push(r);n=r.parent();n.css({position:"relative",overflow:"hidden",padding:0,margin:0})}else{r=m[0];n=r.parent()}function o(y){if(typeof(y.preventDefault)=="function"){y.preventDefault()}}function f(){if(m.length>1){var y=m.pop(),z=r;m=[];m.push(z);if(y!=z){y.triggerHandler(p);z.triggerHandler(b);y.triggerHandler(u);z.triggerHandler(j)}}}function x(){if(m.length>1){var y=m.pop(),z=m.pop();if(z!=undefined){m.push(z);y.triggerHandler(p);z.triggerHandler(b);y.triggerHandler(u);z.triggerHandler(j)}else{m.push(y)}}}function h(){var y=a(this);y.find(i.sub).addClass(i.hidden);d(y);y.trigger(i.live.utrigger)}function k(){var y=this;setTimeout(function(){y.removeClass(i.iframe);if(y.attr("src")!=undefined){y.toTag("iframe").css({padding:0,border:0})}},Appanel.milli(i.sweep.onNextIn.duration))}function d(y){y.find(i.next).each(function(z,A){if(A.isDrilldownNext==undefined){A.isDrilldownNext=true;a(A).on(q,function(D){o(D);var C=m.pop(),E=false;if(this.drilldownSub==undefined){var B=a(this).siblings(i.sub);if(B.length==0){return}E=B.first();v(E);this.drilldownSub=E;i.live.scroller=E;E.live(i.live)}else{E=this.drilldownSub}if(C.is(E)){m.push(C);return}m.push(C);m.push(E);C.triggerHandler(p);E.triggerHandler(b);C.triggerHandler(s);E.triggerHandler(g)})}});y.find(i.home).each(function(z,A){if(A.isDrilldownBack==undefined){A.isDrilldownBack=true;a(A).on(q,function(B){o(B);f()})}});y.find(i.back).each(function(A,z){if(z.isDrilldownBack==undefined){z.isDrilldownBack=true;a(z).on(q,function(B){o(B);x()})}});y.on("drilldown:home",f).on("drilldown:back",x)}function v(y){var z=y.get(0);if(z.isDrilldown==undefined){z.isDrilldown=true;z.drilldownOptions=i;z.drilldownHistory=m;if(z.isDrilldownRoot=y.is(r)){y.find(i.sub).addClass(i.hidden);i.live.utrigger=i.live.trigger;i.live.trigger+="d";n.on(i.live.trigger,function(A){h.call(A.target);A.stopPropagation()})}else{n.append(y)}d(y.css({position:"absolute",top:0,left:0,width:"100%",height:"100%",padding:0,margin:0}).sweep(w))}}v(e);return this};a.fn.folder=function(b){var l=false,f=!l,k="folder:open",d="folder:close",p="click touchstart";if(typeof b==="string"){l=b}else{if(b===undefined){b={}}else{if(b.context instanceof jQuery){f=b.context;var e,c=f.length,h=this.length;for(e=0;e<h;e++){f[e+c]=this[e]}f.length+=h}}}if(f===true){f=this}function n(i){if(i===undefined){return}if(typeof(i.preventDefault)=="function"){i.preventDefault()}if(typeof(i.stopPropagation)=="function"){i.stopPropagation()}}function o(r){n(r);var t=this.folderOptions,i=t.sweep.onClose,s;if(!t.isOpen){return}if(i.handled==undefined){i.handled=true;i.foldable.on=d;s=[];if(t.open.length>0){i.open.selector=t.open;if(i.open.duration==undefined){i.open.duration=t.duration}s.push(i.open)}if(t.close.length>0){i.close.selector=t.close;if(i.close.duration==undefined){i.close.duration=t.duration}s.push(i.close)}if(s.length>0){i.foldable.sweep=s}t.foldable.sweep(i.foldable)}t.isOpen=false;t.foldable.triggerHandler(d)}function g(r){n(r);var t=this.folderOptions,i=t.sweep.onOpen,s;if(t.isOpen){return}if(i.handled==undefined){i.handled=true;i.foldable.on=k;s=[];if(t.open.length>0){i.open.selector=t.open;if(i.open.duration==undefined){i.open.duration=t.duration}s.push(i.open)}if(t.close.length>0){i.close.selector=t.close;if(i.close.duration==undefined){i.close.duration=t.duration}s.push(i.close)}if(s.length>0){i.foldable.sweep=s}t.foldable.sweep(i.foldable);setTimeout(function(){t.foldable.live(t.live)},i.foldable.duration)}if(t.single){t.context.each(function(u,v){if(v.folderOptions.isOpen){o.call(v)}})}t.isOpen=true;t.foldable.triggerHandler(k)}function m(i){a(i).animate({opacity:"toggle"},Appanel.milli(i.folderOptions.duration))}function q(i){a(i).animate({height:"toggle"},Appanel.milli(i.folderOptions.duration))}function j(u){u.isFolder=true;var s=a(u),v={foldable:{run:q},open:{run:m},close:{run:m}},w=a.extend({single:false,sub:".folder",foldable:".foldable",open:".open",close:".close",live:{trigger:"folder:live"},duration:0.4},b,{context:f,isOpen:false});w.sweep=b.sweep===undefined?{onOpen:a.extend({},v),onClose:v}:{onOpen:b.sweep.onOpen===undefined?a.extend({},v):a.extend({},v,b.sweep.onOpen),onClose:b.sweep.onClose===undefined?v:a.extend(v,b.sweep.onClose)};w.live.utrigger=w.live.trigger;w.live.trigger+="d";if(s.is(f.first())){s.parent().on(w.live.trigger,{options:w},function(y){var A=y.data.options,B=a.extend({},b),x=a(y.target),z=x.find(A.sub);B.context=undefined;z.folder(B);x.trigger(A.live.utrigger);y.stopPropagation()})}w.element=u;var r="ignored",t="."+r,i=s.find(w.sub+" "+w.foldable+","+w.sub+" "+w.open+","+w.sub+" "+w.close);i.addClass(r);w.foldable=s.is(w.foldable)?s:s.find(w.foldable).not(t);w.open=s.find(w.open).not(t);w.close=s.find(w.close).not(t);i.removeClass(r);u.folderOptions=w;w.foldable.each(function(x,y){y.folderOptions=w});w.open.each(function(x,y){y.folderOptions=w;a(y).on(p,g)});w.close.each(function(x,y){y.folderOptions=w;a(y).on(p,o)})}return this.each(function(r,s){if(s.isFolder===undefined){j(s)}if(l){if(l==="open"){g.call(s)}else{if(l==="close"){o.call(s)}}}})}})(jQuery);