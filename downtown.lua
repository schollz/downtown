-- downtown v0.1.0
-- the cityscape is full of sound.
--
-- llllllll.co/t/downtown
--
--
--
--    ▼ instructions below ▼
--
-- K2 switches sample
-- K3 toggles recording
-- E2 changes modulator
-- E3 modulates

local Formatters=require 'formatters'

include("lib/pixels") -- global functions

engine.name="Downtown"

loop_max_beats=32

-- these show up as "towers" which you can scale
-- if you change the engine you should change these
modulators={
  -- these are engine related (see the engine)
  {name="storm",max=0.6},
  {name="powerline",max=0.5},
  {name="birds",max=1.0},
  {name="bells",max=1.0},
  {name="pulse",max=0.5},
  {name="pulsenote",min=12,max=60,interval=1,default=29,natural=true},
  {name="snare",max=0.5},
  {name="kick",max=0.5},
}

-- state
update_ui=false
softcut_loop_starts={1,1,1,1,1,1}
softcut_loop_ends={60,60,60,60,60,60}
modulator_ordering={}
ui_choice_sample=0
ui_choice_mod=0
debounce_params_save=0
debounce_set_max_beats=0
sprite_positions={}
sprite_view_order={}
param_id_to_index={}

-- WAVEFORMS
waveform_samples={{}}
current_positions={1,1,1,1,1,1}
-- drawing
star_positions={}
bar_position=20
waveform_height=26
bar_height=5
ui_show_flames=false
ui_enc3_on=0

function init()
  norns.enc.sens(2,4)
  norns.enc.sens(3,4)
  
  -- setup the running clock
  updater=metro.init()
  updater.time=0.2
  updater.count=-1
  updater.event=update_screen
  updater:start()
  
  -- setup parametesr
  setup_parameters()
  
  -- initialize softcut (after parameters)
  reset_softcut()
  softcut.event_phase(update_positions)
  softcut.event_render(on_render)
  softcut.poll_start_phase()

  -- TODO load default parameters
  params:bang()
  
  -- set the bpm
  engine.bpm(clock.get_tempo())
  
  -- setup audio
  audio.level_eng_cut(0)
  audio.level_adc_cut(1)
  audio.level_tape_cut(1)

  -- sprite star positions
  star_positions={}
  for i=1,math.random(15,30) do
    star_positions[i]={math.random(1,110),math.random(1,19),math.random(1,15),math.random(1,10)/10}
  end
  -- update sprite positions
  update_sprite_positions()

  params:set("file_fr1","/home/we/dust/audio/tehn/mancini1.wav")
end

function update_sprite_positions()
  sprite_positions = {}
  for i,m in ipairs(modulators) do 
    table.insert(sprite_positions,{"sc"..i,params:get("sprite_pos_sc"..i)})
  end
  for i=1,8 do 
    if not string.find(params:get("file_fr"..i),"silence.wav") and string.find(params:get("file_fr"..i),".wav") then 
      table.insert(sprite_positions,{"fr"..i,params:get("sprite_pos_fr"..i)})
    end
  end
  for i=1,3 do 
    table.insert(sprite_positions,{"loop"..i,100+7*i})
  end
  table.sort(sprite_positions,compare_second)
  sprite_view_order = {}
  for i,m in ipairs(modulators) do 
    table.insert(sprite_view_order,{"sc"..i,params:get("sprite_zorder_sc"..i)})
  end
  for i=1,8 do 
    if not string.find(params:get("file_fr"..i),"silence.wav") and string.find(params:get("file_fr"..i),".wav") then 
      table.insert(sprite_view_order,{"fr"..i,params:get("sprite_zorder_fr"..i)})
    end
  end
  for i=1,3 do 
    table.insert(sprite_view_order,{"loop"..i,params:get("sprite_zorder_loop"..i)})
  end
  table.sort(sprite_view_order,compare_second)
end

function compare_second(a,b)
  return a[2] < b[2]
end

function setup_parameters()
  params:add{type="control",id="maxbeats",name="max beats",controlspec=controlspec.new(1,128,"lin",1,16,"beats"),
      action=function(value)
        debounce_set_max_beats=4
      end
  }

  -- add supercollider params + sprites
  params:add_separator("supercollider")
  for i,m in ipairs(modulators) do
    params:add_group(m.name,4)
    params:add{type="control",id="sc"..i,name=m.name,controlspec=controlspec.new(m.min,m.max,"lin",m.interval,m.default,""),
      action=function(value)
        local enginecmd="engine."..m.name.."("..value..")"
        local f=load(enginecmd)
        f()
        debounce_params_save=3
      end
    }
    isnatural = 1 
    if m.natural ~= nil and m.natural then 
      isnatural = 2
    end
    params:add{type="option",id="natural_sc"..i,name="natural",options={"no","yes"},default=isnatural,
      action=function(value)
        debounce_params_save=3
      end
    }
    params:add{type="option",id="loves_sc"..i,name="loves gojira",options={"no","yes"},default=1,
      action=function(value)
        debounce_params_save=3
      end
    }
    params:add{type="option",id="fears_sc"..i,name="fears gojira",options={"no","yes"},default=2,
      action=function(value)
        debounce_params_save=3
      end
    }
    -- parameters for drawing
    params:add_text("sprite_name_sc"..i)
    params:set("sprite_name_sc"..i,m.name)
    params:hide("sprite_name_sc"..i)
    params:add_number("sprite_zorder_sc"..i)
    params:set("sprite_zorder_sc"..i,math.random(1,15))
    params:hide("sprite_zorder_sc"..i)
    params:add_number("sprite_width_sc"..i)
    params:set("sprite_width_sc"..i,util.clamp(gaussian(9,6),3,22))
    params:hide("sprite_width_sc"..i)
    params:add_number("sprite_density_sc"..i)
    params:set("sprite_density_sc"..i,math.random(20,90)/100.0)
    params:hide("sprite_density_sc"..i)
    params:add_number("sprite_xspacing_sc"..i)
    params:set("sprite_xspacing_sc"..i,math.random(2,4))
    params:hide("sprite_xspacing_sc"..i)
    params:add_number("sprite_xspacing2_sc"..i)
    params:set("sprite_xspacing2_sc"..i,math.random(1,2))
    params:hide("sprite_xspacing2_sc"..i)
    params:add_number("sprite_yspacing_sc"..i)
    params:set("sprite_yspacing_sc"..i,math.random(2,4))
    params:hide("sprite_yspacing_sc"..i)
    params:add_number("sprite_num_sc"..i)
    params:set("sprite_num_sc"..i,math.random(1,15))
    params:hide("sprite_num_sc"..i)
    params:add_number("sprite_pos_sc"..i)
    params:set("sprite_pos_sc"..i,math.floor(i*80/#modulators+4+math.random(-1,1)))
    params:hide("sprite_pos_sc"..i)
  end
  

  -- add field recording params + sprites
  params:add_separator("field recordings")
  for i=1,8 do 
    params:add_group("field recording "..i,5)
    params:add_file("file_fr"..i,"file","/home/we/dust/audio/")
    params:set_action("file_fr"..i,function(value)
        if string.find(value,".wav") then 
          local enginecmd="engine.sample"..i.."file('"..value.."')"
          print(enginecmd)
          local f=load(enginecmd)
          f()
          update_sprite_positions() -- adding a new sprite in this case
          debounce_params_save=3
        end
    end)
    params:add{type="control",id="fr"..i,name="amp",controlspec=controlspec.new(0,1,"lin",0.01,0,""),
      action=function(value)
        local enginecmd="engine.sample"..i.."("..value..")"
        local f=load(enginecmd)
        f()
        debounce_params_save=3
      end
    }
    params:add{type="option",id="natural_fr"..i,name="natural",options={"no","yes"},default=2,
      action=function(value)
        debounce_params_save=3
      end
    }
    params:add{type="option",id="loves_fr"..i,name="loves gojira",options={"no","yes"},default=2,
      action=function(value)
        debounce_params_save=3
      end
    }
    params:add{type="option",id="fears_fr"..i,name="fears gojira",options={"no","yes"},default=1,
      action=function(value)
        debounce_params_save=3
      end
    }
    -- parameters for drawing
    params:add_text("sprite_name_fr"..i)
    params:set("sprite_name_fr"..i,"field"..i)
    params:hide("sprite_name_fr"..i)
    params:add_number("sprite_zorder_fr"..i)
    params:set("sprite_zorder_fr"..i,math.random(1,15))
    params:hide("sprite_zorder_fr"..i)
    params:add_number("sprite_width_fr"..i)
    params:set("sprite_width_fr"..i,util.clamp(gaussian(12,6),3,30))
    params:hide("sprite_width_fr"..i)
    params:add_number("sprite_density_fr"..i)
    params:set("sprite_density_fr"..i,math.random(20,90)/100.0)
    params:hide("sprite_density_fr"..i)
    params:add_number("sprite_xspacing_fr"..i)
    params:set("sprite_xspacing_fr"..i,math.random(2,4))
    params:hide("sprite_xspacing_fr"..i)
    params:add_number("sprite_xspacing2_fr"..i)
    params:set("sprite_xspacing2_fr"..i,math.random(1,2))
    params:hide("sprite_xspacing2_fr"..i)
    params:add_number("sprite_yspacing_fr"..i)
    params:set("sprite_yspacing_fr"..i,math.random(2,4))
    params:hide("sprite_yspacing_fr"..i)
    params:add_number("sprite_num_fr"..i)
    params:set("sprite_num_fr"..i,math.random(1,15))
    params:hide("sprite_num_fr"..i)
    params:add_number("sprite_pos_fr"..i)
    params:set("sprite_pos_fr"..i,math.floor((i-1)*9+9))
    params:hide("sprite_pos_fr"..i)
  end

  -- parameters for softcut loops
  params:add_separator("softcut loops")
  for i=1,3 do
    params:add_group("loop "..i,9)
    params:add {type="control",id="loop"..i,name="level",controlspec=controlspec.new(0,0.5,'lin',0.01,0.5-(i/6),''),
      action=function(value)
        softcut.level(i*2,value)
        softcut.level(i*2-1,value)
        debounce_params_save=3
      end
    }
    params:add {type='control',id=i..'start',name='start',controlspec=controlspec.new(1,loop_max_beats-1,'lin',1,1,'beats'),
      action=function(value)
        local loop_length=clock.get_beat_sec()*(value-1)
        softcut.loop_start(i*2,softcut_loop_starts[i*2]+loop_length)
        softcut.loop_start(i*2-1,softcut_loop_starts[i*2-1]+loop_length)
        debounce_params_save=3
      end
    }
    params:add {type='control',id=i..'end',name='end',controlspec=controlspec.new(1,loop_max_beats,'lin',1,16,'beats'),
      action=function(value)
        local loop_length=clock.get_beat_sec()*value
        softcut.loop_end(i*2,softcut_loop_starts[i*2]+loop_length)
        softcut.loop_end(i*2-1,softcut_loop_starts[i*2-1]+loop_length)
        debounce_params_save=3
      end
    }
    params:add {type='control',id=i..'erase',name='erase (each loop)',controlspec=controlspec.new(0,100,'lin',1,0,'%'),
      action=function(value)
        softcut.pre_level(i*2,(1-value/100))
        softcut.pre_level(i*2-1,(1-value/100))
        debounce_params_save=3
      end
    }
    params:add{type='binary',name="record",id=i..'rec',behavior='toggle',
      action=function(v)
        softcut.rec_level(i*2,v)
        softcut.rec_level(i*2-1,v)
        debounce_params_save=3
      end
    }
    params:add {type='control',id=i..'filter_frequency',name='filter cutoff',controlspec=controlspec.new(20,20000,'exp',0,20000,'Hz',100/20000),formatter=Formatters.format_freq,
      action=function(value)
        softcut.post_filter_fc(i*2,value)
        softcut.post_filter_fc(i*2-1,value)
        debounce_params_save=3
      end
    }
    params:add{type='binary',name="reset",id=i..'reset',behavior='trigger',
      action=function(v)
        softcut.position(i*2,softcut_loop_starts[i*2])
        softcut.position(i*2-1,softcut_loop_starts[i*2-1])
        debounce_params_save=3
      end
    }
    params:add{type="option",id="natural_loop"..i,name="natural",options={"no","yes"},default=1,
      action=function(value)
        debounce_params_save=3
      end
    }
    params:add{type="option",id="loves_loop"..i,name="loves gojira",options={"no","yes"},default=1,
      action=function(value)
        debounce_params_save=3
      end
    }
    params:add{type="option",id="fears_loop"..i,name="fears gojira",options={"no","yes"},default=2,
      action=function(value)
        debounce_params_save=3
      end
    }
    -- parameters for drawing
    params:add_text("sprite_name_loop"..i)
    params:set("sprite_name_loop"..i,"loop"..i)
    params:hide("sprite_name_loop"..i)
    params:add_number("sprite_zorder_loop"..i)
    params:set("sprite_zorder_loop"..i,math.random(1,15))
    params:hide("sprite_zorder_loop"..i)
    params:add_number("sprite_width_loop"..i)
    params:set("sprite_width_loop"..i,util.clamp(gaussian(12,6),3,30))
    params:hide("sprite_width_loop"..i)
    params:add_number("sprite_density_loop"..i)
    params:set("sprite_density_loop"..i,math.random(20,90)/100.0)
    params:hide("sprite_density_loop"..i)
    params:add_number("sprite_xspacing_loop"..i)
    params:set("sprite_xspacing_loop"..i,math.random(2,4))
    params:hide("sprite_xspacing_loop"..i)
    params:add_number("sprite_xspacing2_loop"..i)
    params:set("sprite_xspacing2_loop"..i,math.random(1,2))
    params:hide("sprite_xspacing2_loop"..i)
    params:add_number("sprite_yspacing_loop"..i)
    params:set("sprite_yspacing_loop"..i,math.random(2,4))
    params:hide("sprite_yspacing_loop"..i)
    params:add_number("sprite_num_loop"..i)
    params:set("sprite_num_loop"..i,math.random(1,15))
    params:hide("sprite_num_loop"..i)
    params:add_number("sprite_pos_loop"..i)
    params:set("sprite_pos_loop"..i,math.floor(80+i*7))
    params:hide("sprite_pos_loop"..i)
  end

  for i,p in ipairs(params.params) do 
    if p.id ~= nil then 
      param_id_to_index[p.id]=i
    end
  end
end

function update_positions(i,x)
  current_positions[i]=x
end

function update_screen()
  if debounce_params_save > 0 then 
    debounce_params_save = debounce_params_save - 1
    if debounce_params_save == 0 then 
      -- TODO: save
    end
  end
  if debounce_set_max_beats > 0 then 
    debounce_set_max_beats = debounce_set_max_beats - 1
    if debounce_set_max_beats == 0 then 
      reset_softcut()
      -- save parameters
    end
  end
  if clock.get_beats()-ui_enc3_on>2 and ui_show_flames then
    ui_show_flames=false
  elseif ui_show_flames==false and clock.get_beats()-ui_enc3_on<2 then
    ui_show_flames=true
  end
  softcut.render_buffer(1,1,clock.get_beat_sec()*loop_max_beats*3+1,128)
  softcut.render_buffer(2,1,clock.get_beat_sec()*loop_max_beats*3+1,128)
  redraw()
end

function on_render(ch,start,i,s)
  waveform_samples[ch]=s
end

function reset_softcut()
  loop_start=1
  loop_length=clock.get_beat_sec()*params:get("maxbeats")
  softcut.reset()
  softcut.buffer_clear()
  for i=1,6 do
    j = math.ceil(i/2) -- which of the 3 stereo
    softcut.enable(i,1)
    
    softcut.level(i,params:get("loop"..j))
    if i%2==1 then
      softcut.pan(i,1)
      softcut.buffer(i,1)
      softcut.level_input_cut(1,i,1)
      softcut.level_input_cut(2,i,0)
    else
      softcut.pan(i,-1)
      softcut.buffer(i,2)
      softcut.level_input_cut(1,i,0)
      softcut.level_input_cut(2,i,1)
    end
    
    softcut.rec(i,1)
    softcut.play(i,1)
    softcut.rate(i,1)
    softcut.loop_start(i,loop_start)
    softcut.loop_end(i,loop_start+loop_length)
    softcut_loop_starts[i]=loop_start
    softcut_loop_ends[i]=loop_start+loop_length
    softcut.loop(i,1)
    
    softcut.level_slew_time(i,0.4)
    softcut.rate_slew_time(i,0.4)
    
    softcut.rec_level(i,0.0)
    softcut.pre_level(i,1.0)
    softcut.position(i,loop_start)
    softcut.phase_quant(i,0.025)
    
    softcut.post_filter_dry(i,0.0)
    softcut.post_filter_lp(i,1.0)
    softcut.post_filter_rq(i,1.0)
    softcut.post_filter_fc(i,params:get(j..'filter_frequency'))
    
    softcut.pre_filter_dry(i,1.0)
    softcut.pre_filter_lp(i,1.0)
    softcut.pre_filter_rq(i,1.0)
    softcut.pre_filter_fc(i,20100)
    -- iterate
    if i==2 or i==4 then
      loop_start=loop_start+loop_length+0.5
    end
  end
end

function enc(k,d)
  if k==2 then
    ui_choice_mod=sign_cycle(ui_choice_mod,d,0,#sprite_positions)
  elseif k==3 and ui_choice_mod>0 then
    id = sprite_positions[ui_choice_mod][1]
    index = param_id_to_index[id]
    interval = params.params[index].controlspec.step
    if interval == 0 then 
      interval = 0.01
    end
    if interval==1 then
      d=sign(d)
    end
    params:set(id,params:get(id)+d*interval)
  elseif k==1 then
    -- TODO do godzilla
    -- ui_enc3_on=clock.get_beats()
    -- for i,m in ipairs(modulators) do
    --   if params:get(m.name)>0 then
    --     if m.interval==1 then
    --       d=sign(d)
    --     end
    --     params:set(m.para,util.clamp(params:get(m.para)+d*m.interval,m.min,m.max))
    --   end
    -- end
  end
end

function key(k,z)
  if k==2 and z==1 then
    ui_choice_sample=sign_cycle(ui_choice_sample,z,0,3)
  elseif k==3 and z==1 and ui_choice_sample>0 then
    params:set(ui_choice_sample.."rec",1-params:get(ui_choice_sample.."rec"))
  end
end


function redraw()
  screen.clear()
  
  -- draw engine skyline
  draw_stars()
  draw_moon()
  for i,sp in ipairs(sprite_view_order) do 
    draw_sprite(sp[1],false)
  end
  draw_godzilla()
  
  -- show samples
  screen.level(15)
  local positions={}
  for i,p in ipairs(current_positions) do
    local frac=math.ceil(i/2-1)/3
    positions[i]=util.round(util.linlin(softcut_loop_starts[i],softcut_loop_ends[i],math.ceil(i/2-1)/3*128,math.ceil(i/2)/3*128,p))
  end
  if waveform_samples[1]~=nil and waveform_samples[2]~=nil then
    for j=1,2 do
      for i,s in ipairs(waveform_samples[j]) do
        if i==1 or i==1+42 or i==1+42+42 or i>=127 then
          goto continue
        end
        local highlight=false
        if i==positions[1] or i==positions[2] or i==positions[3] or i==positions[4] or i==positions[5] or i==positions[6] then
          highlight=true
        end
        for k=1,3 do
          if params:get(k.."rec")==1 and i>42*(k-1) and i<=42*k then
            highlight=not highlight
            break
          end
        end
        if highlight then
          screen.level(15)
        else
          screen.level(1)
        end
        local height=util.clamp(0,waveform_height,util.round(math.abs(s)*waveform_height))
        screen.move(i,58-waveform_height/2)
        screen.line_rel(0,(j*2-3)*height)
        screen.stroke()
        ::continue::
      end
    end
  end
  
  -- draw rect around current sample
  if ui_choice_sample>0 then
    if params:get(ui_choice_sample.."rec")==1 then
      screen.level(15)
    else
      screen.level(1)
    end
    screen.rect(1+42*(ui_choice_sample-1),bar_position+bar_height+1,43,64-bar_position-bar_height-1)
    screen.stroke()
  end
  
  -- draw middle bar
  screen.level(15)
  screen.rect(0,bar_position-1,128,bar_height+2)
  screen.fill()
  screen.level(0)
  
  -- label which thing is selected
  if ui_choice_mod>0 then
    id=sprite_positions[ui_choice_mod][1]
    x = params:get("sprite_pos_"..id)
    y=bar_position+bar_height
    w=params:get("sprite_width_"..id)
    if params:get("natural_"..id)==2 then 
      w = 18
    end
    screen.level(0)
    -- if ui_choice_mod>=#sprite_positions-1 then
    --   screen.move(x+w,y)
    --   screen.text_right(params:get("sprite_name_"..id))
    -- if ui_choice_mod<=2 then
    --   screen.move(x,y)
    --   screen.text(params:get("sprite_name_"..id))
    -- else
    --   screen.move(x+w/2,y)
    --   screen.text_center(params:get("sprite_name_"..id))
    -- end
    screen.move(x+w/2,y)
    screen.text_center(params:get("sprite_name_"..id))
  end
  draw_boom()
  screen.update()
end

--
-- drawings
--

function draw_sprite(id,highlight)
  v = params:get(id)
  if v == 0 then 
    do return end 
  end
  index = param_id_to_index[id]
  x = params:get("sprite_pos_"..id)
  v = (v-params.params[index].controlspec.minval) / (params.params[index].controlspec.maxval-params.params[index].controlspec.minval)
  if params:get("natural_"..id) == 2 then 
    -- draw tree 
    draw_tree(x,v,params:get("sprite_num_"..id),params:get("sprite_zorder_"..id),highlight)
  else
    -- draw building 
    draw_building(x,v,params:get("sprite_width_"..id),params:get("sprite_zorder_"..id),params:get("sprite_xspacing_"..id),params:get("sprite_xspacing2_"..id),params:get("sprite_yspacing_"..id),params:get("sprite_density_"..id),highlight)
  end
end

function draw_building(x,v,w,zorder,xspacing,xspacing2,yspacing,density)
  y=bar_position
  h=bar_position-1
  screen.level(0)
  screen.rect(x,y-v*h,w,v*h)
  screen.fill()
  if highlight then
    screen.level(15)
  else
    screen.level(zorder)
  end
  screen.rect(x,y-v*h,w,v*h)
  screen.stroke()
  
  x=math.floor(x)
  w=math.floor(w)
  y=math.floor(y-v*h)
  vh=math.floor(v*h)
  xpos=x+xspacing-xspacing2
  ypos=y+yspacing
  math.randomseed(x)
  while ypos<y+vh do
    if xpos>=x+w then
      ypos=ypos+yspacing
      xpos=x+xspacing-xspacing2
    else
      if math.random()<density then
        if xpos==x+w-1 then
          screen.pixel(xpos-1,ypos)
        else
          screen.pixel(xpos,ypos)
        end
        screen.fill()
      end
      xpos=xpos+xspacing
    end
  end
end

function draw_stars()
  for i,p in ipairs(star_positions) do
    star_positions[i][3]=star_positions[i][3]+star_positions[i][4]
    if star_positions[i][3]>15 then
      star_positions[i][3]=1
    end
    screen.level(math.floor(star_positions[i][3]))
    screen.pixel(p[1],p[2])
    screen.fill()
  end
end


function draw_tree(x,v,tree_num,zorder)
  height=(1-v)*18
  screen.level(zorder)
  for _,p in ipairs(tree_pixels[tree_num].dark) do
    if p[2]+height>height and p[2]+height<19 then
      screen.pixel(p[1]+x,p[2]+height)
    end
  end
  screen.fill()
  screen.level(0)
  for _,p in ipairs(tree_pixels[tree_num].light) do
    if p[2]+height>height and p[2]+height<19 then
      screen.pixel(p[1]+x,p[2]+height)
    end
  end
  screen.fill()
end

function draw_boom()
  if not ui_show_flames then
    do return end
  end
  math.randomseed(math.ceil(clock.get_beats()*1000))
  i=math.random(1,2)
  screen.level(15)
  for _,p in ipairs(fire_pixels[i].dark) do
    screen.pixel(p[1],p[2])
  end
  screen.fill()
  screen.level(0)
  for _,p in ipairs(fire_pixels[i].light) do
    screen.pixel(p[1],p[2])
  end
  screen.fill()
end


function draw_moon()
  local pixels={
    {14,0},{15,0},{16,0},{13,1},{14,1},{15,1},{12,2},{13,2},{14,2},{12,3},{13,3},{14,3},{12,4},{13,4},{14,4},{12,5},{13,5},{14,5},{15,5},{12,6},{13,6},{14,6},{15,6},{16,6},{17,6},{18,6},{13,7},{14,7},{15,7},{16,7},{17,7},
  }
  screen.level(8)
  for i,p in ipairs(pixels) do
    screen.pixel(p[1],p[2])
  end
  screen.fill()
end

function draw_godzilla()
  local pixels={
    {114,0},{115,0},{116,0},{117,0},{118,0},{119,0},{120,0},{121,0},{122,0},{112,1},{113,1},{122,1},{124,1},{125,1},{126,1},{112,2},{119,2},{122,2},{123,2},{124,2},{126,2},{112,3},{113,3},{114,3},{115,3},{116,3},{117,3},{124,3},{126,3},{127,3},{128,3},{129,3},{117,4},{124,4},{125,4},{126,4},{129,4},{115,5},{116,5},{126,5},{127,5},{129,5},{114,6},{127,6},{128,6},{129,6},{130,6},{114,7},{115,7},{116,7},{117,7},{118,7},{123,7},{128,7},{129,7},{119,8},{123,8},{129,8},{130,8},{120,9},{121,9},{123,9},{130,9},{119,10},{120,10},{119,11},{121,11},{122,11},{122,12},{122,13},{123,13},{128,13},{129,13},{123,14},{124,14},{125,14},{127,14},{128,14},{122,15},{123,15},{127,15},{121,16},{127,16},{120,17},{121,17},{122,17},{126,17},{127,17},{128,17},{129,17},
  }
  screen.level(5)
  if ui_choice_mod==0 then
    screen.level(10)
  end
  for _,p in ipairs(pixels) do
    screen.pixel(p[1],p[2]+1)
  end
  screen.fill()
end

function sign(value)
  if value>0 then
    return 1
  elseif value<0 then
    return-1
  end
  return 0
end

function sign_cycle(value,d,min,max)
  if d>0 then
    value=value+1
  elseif d<0 then
    value=value-1
  end
  if value>max then
    value=min
  elseif value<min then
    value=max
  end
  return value
end

function gaussian (mean,variance)
  return math.sqrt(-2*variance*math.log(math.random()))*
  math.cos(2*math.pi*math.random())+mean
end

function rerun()
  norns.script.load(norns.state.script)
end
