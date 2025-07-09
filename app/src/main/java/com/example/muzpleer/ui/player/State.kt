package com.example.muzpleer.ui.player

sealed class State (
   val  IsPlaying:State,
   val  IsStop:State,
   val  IsPause:State
)