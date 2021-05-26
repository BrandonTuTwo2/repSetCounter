/*
* Creator: Brandon Tu
*
* Description: An android app that tracks how long a rep should be, how long a rest should be and how
* many reps to do, useful when working out doing timed reps and doing HIIT workouts
*
* Latest Updated Date: 2021 05 26
*
* Additional Notes: you can totally use this app, change it, use parts of the code just credit me and
* the other references if you choose to keep using the other resources :D
*
* References:
*       Energize - No More Heroes produced by Masafumi Takada labelled by Pioneer Records
*       Final Round Sound - Ultra Street Fighter 4 composed/created by Hideyuki Fukasawa
*       GAME sound effect - Super Smash Bros Melee composed/created by Hirokazu Tanaka
*       Ready Go sound effect - Super Smash Bros Melee composed/created by Hirokazu Tanaka
*       The idea of calling the timer function again was gotten from here to credits to them: https://stackoverflow.com/questions/58900722/creating-multiple-countdown-timers-with-a-for-loop-android-kotlin
*       Background by Aliaksei Brouka gotten from here: https://www.123rf.com/profile_nexusby?mediapopup=63188741
*/


package com.example.repsetcounter

import android.os.Bundle
import android.os.CountDownTimer
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import android.util.Log
import android.media.MediaPlayer






class MainActivity : AppCompatActivity() {
    //variables for the sounds effects
    private var finishSound: MediaPlayer? = null;
    private var startSound: MediaPlayer? = null;
    private var lastRepSound: MediaPlayer? = null;
    private var finishRep: MediaPlayer? = null;

    //variables for the other buttons and their functions
    private var reset : Boolean = false;
    private var pause : Boolean = false;
    private var isRunning : Boolean = false;

    //saved copies of the time so it can restart again after pausing
    private var remainingTime : Long = 0;
    private var savedCurrentRep : Int = 0;
    private var savedTotalRep : Int = 0;
    private var savedRepInterval : Long = 0;
    private var savedRestInterval : Long = 0;
    private var savedOption : Int = 0;
    private var savedRepView : Int = 0;

    //looks redundant but I kept it that way so its easier to read and understand
    //basically checks if the minutes and seconds are below 60 (they cant input anything negative so its only to make sure its below 60)
    private fun checkValidTime(repMin : Int, repSec : Int, restMin : Int, restSec : Int) : Boolean{
        if(repMin > 60 || repSec > 60) {
            return false;
        } else if(restMin > 60 || restSec > 60) {
            return false;
        } else if(repMin == 0 && repSec == 0){
            return false;
        } else if(restMin == 0 && restSec == 0) {
            return false;
        } else {
            return true;
        }
     }

    //0 for workout 1 for rest
    //this function essentially hands all the timer logic
    //it takes how many total reps, the current rep, how long a rep interval is, how long a rest interval is and so on
    private fun timerStart(currentRep : Int, totalRep : Int, currentTimeView : TextView, convertedRepInterval : Long, convertedRestInterval : Long, option : Int, currentRepView : TextView, repViewInt : Int, statusChange : TextView) {
        var newCurrentIter : Int = currentRep;
        var startFix : Int =  0;
        var optionSize: Long = 0;
        var newOption : Int = option;
        var newRepViewInt : Int = repViewInt;
        var secondsRemaining : Int = 0;
        var minutesRemaining : Int = 0;

        //depending on the current option (if its rest or rep) it uses that interval length
        if(option == 0) {
            optionSize = convertedRepInterval;
            newRepViewInt++;
            currentRepView.text = newRepViewInt.toString();
            statusChange.text = "rep";
        } else {
            statusChange.text = "rest";
            optionSize = convertedRestInterval;
        }

        //this is for restarting the timer after pausing the interval time becomes the saved remaining time
        if(remainingTime.compareTo(0) != 0) {
            optionSize = remainingTime;
            remainingTime = 0;
        }

        //this is the main timer object
        //countDownTimer seems to be Async so I had some issues with setting up the pause, play and reset functions
        var timer = object: CountDownTimer(optionSize,1000) {
            override fun onTick(millisUntilFinished:Long) {
                //this is just displaying the remaining time to the user
                secondsRemaining = ((millisUntilFinished % 60000)/1000).toInt();
                minutesRemaining = (millisUntilFinished/60000).toInt();
                currentTimeView.text = "%02d".format(minutesRemaining) + ":" + "%02d".format(secondsRemaining);

                //since its async the object continues to listen to any changes from the global boolean variables
                //if the reset is hit then it cancels the timer and sets things back to zero
                //if pauses is clicked then it saves the relevant variables to be used later
                 if(reset) {
                    reset = false;
                    this.cancel();
                    currentTimeView.text = "00:00";
                    currentRepView.text = "0";
                } else if(pause) {
                     remainingTime = millisUntilFinished;
                     savedCurrentRep = currentRep;
                     savedTotalRep = totalRep;
                     savedRepInterval = convertedRepInterval;
                     savedRestInterval = convertedRestInterval;
                     savedOption = option;
                     savedRepView = repViewInt;
                     currentTimeView.text = "paused";
                    this.cancel();
                }

                isRunning = true;
                //plays the sound effects near the end
                 if(minutesRemaining == 0 && secondsRemaining == 2 && option == 1 && newCurrentIter != (totalRep-2)) {
                    startSound!!.start();
                } else if(minutesRemaining == 0 && secondsRemaining == 1 && option == 0 && newCurrentIter != (totalRep-2)) {
                     finishRep!!.start();
                 }
            }
            //when the timer is done, it increments the current iteration and if its not equal to the amount of reps it calls this function again kind of recursively
            //until current reps <= total Reps
            override fun onFinish() {
                isRunning = false;
                newCurrentIter++;
                if(newCurrentIter <= (totalRep-1)) {
                    if(option == 1 && newCurrentIter == (totalRep-1)) {
                        lastRepSound!!.start();
                    }
                    if(newOption == 0) {
                        newOption = 1;
                    } else {
                        newOption = 0;
                    }
                    timerStart(newCurrentIter,totalRep,currentTimeView,convertedRepInterval,convertedRestInterval,newOption,currentRepView,newRepViewInt, statusChange);
                } else {
                    statusChange.text = "-";
                    currentTimeView.text = "Times up!"
                    finishSound!!.start();
                }
            }
        }
        timer.start();
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        var repAmount: Int? = 0;
        var repSizeSeconds: Int? = 0;
        var repSizeMinutes : Int? = 0;
        var restSizeSeconds: Int? = 0;
        var restSizeMinutes : Int? = 0;
        var convertedRepInterval: Long = 0;
        var convertedRestInterval: Long = 0;
        var iteration: Int? = 0;
        var currentRepView = findViewById<TextView>(R.id.currentRep);
        var currentTimeView = findViewById<TextView>(R.id.currentTime);
        var statusChange = findViewById<TextView>(R.id.statusTextView);

        //sets up the sounds to be used
        finishSound = MediaPlayer.create(this, R.raw.game_ssb_soundeffect);
        startSound = MediaPlayer.create(this,R.raw.ready_go_ssb);
        lastRepSound = MediaPlayer.create(this,R.raw.final_round_street_fighter);
        finishRep = MediaPlayer.create(this,R.raw.nmh_energize_edit);

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //this is the start/reset button
        setRepBtn.setOnClickListener{
            var  repAmountText = findViewById<EditText>(R.id.amount);

            repAmount =  repAmountText.text.toString().toIntOrNull();
            currentRepView = findViewById<TextView>(R.id.currentRep);
            currentRepView.text = "1";
            currentTimeView = findViewById<TextView>(R.id.currentTime);

            var repSizeTextSeconds = findViewById<EditText>(R.id.repTimeSeconds);
            var repSizeTextMinutes = findViewById<EditText>(R.id.repTimeMinutes);

            var restSizeTextSeconds = findViewById<EditText>(R.id.restTimeSeconds);
            var restSizeTextMinutes = findViewById<EditText>(R.id.restTimeMinutes);

            //gets the time that the user entered
            repSizeSeconds = repSizeTextSeconds.text.toString().toIntOrNull();
            repSizeMinutes = repSizeTextMinutes.text.toString().toIntOrNull();

            restSizeSeconds = restSizeTextSeconds.text.toString().toIntOrNull();
            restSizeMinutes = restSizeTextMinutes.text.toString().toIntOrNull();

            //if the user didn't input anything and or left it null then it alerts the user and does not do anything
            if(repAmount == 0 || repAmount == null || repSizeMinutes == null || repSizeSeconds == null || restSizeMinutes == null || restSizeSeconds == null) {
                var snack = Snackbar.make(it,"Invalid input please check again",Snackbar.LENGTH_LONG)
                snack.show()
            } else {
                //sets up the variables to be used
                statusChange = findViewById<TextView>(R.id.statusTextView);

                iteration = repAmount;

                if(checkValidTime(repSizeMinutes!!,repSizeSeconds!!,restSizeMinutes!!,restSizeSeconds!!)) {
                    //converts the time to a long/milliseconds and sends it to the timerStart function
                    repSizeMinutes =  repSizeMinutes!!*60;
                    restSizeMinutes = restSizeMinutes!!*60;

                    convertedRepInterval = (repSizeSeconds!!*1000).toLong() + (repSizeMinutes!!*1000).toLong();
                    convertedRestInterval = (restSizeSeconds!!*1000).toLong() + (restSizeMinutes!!*1000).toLong();

                    iteration = iteration?.plus((iteration!!-1));
                    timerStart(0,iteration!!,currentTimeView,convertedRepInterval,convertedRestInterval,0,currentRepView,0, statusChange);
                } else {
                    var notif = Snackbar.make(it,"please enter a number from 1 - 60",Snackbar.LENGTH_LONG)
                    notif.show();
                }
            }

        }

        //sets the reset variable to true and notifies the user if the timer is not running
        resetBtn.setOnClickListener{
            if(isRunning) {
                reset = true;
            } else {
                val notif = Snackbar.make(it,"timer isn't running",Snackbar.LENGTH_LONG)
                notif.show()
            }
        }

        //sets the pause variable to true if its false and vice versa, if pause is true then it takes the saved variables and calls the timer function again
        playPauseBtn.setOnClickListener {
            if(isRunning) {
                if(pause) {
                    timerStart(savedCurrentRep,savedTotalRep,currentTimeView,savedRepInterval,savedRestInterval,savedOption,currentRepView,savedRepView,statusChange)
                    pause = false;
                } else {
                    pause = true;
                }
            } else {
                val notif = Snackbar.make(it,"timer isn't running",Snackbar.LENGTH_LONG)
                notif.show()
            }
        }
    }
}
