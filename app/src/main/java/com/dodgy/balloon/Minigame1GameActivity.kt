package com.dodgy.balloon

import android.os.Bundle
import android.view.View
import android.os.Handler
import android.graphics.Rect
import android.content.Intent
import android.view.MotionEvent
import android.widget.ImageView
import android.media.MediaPlayer
import android.util.DisplayMetrics
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import kotlinx.android.synthetic.main.minigame1game.*

class Minigame1GameActivity: AppCompatActivity() {

    // Holds the animator that is reused to animate all the imageViews
    var animator = ValueAnimator.ofFloat(0f, 0f)!!
    // Creates variable to be turned off for calling of animation to ghost
    var balloonTouched: Boolean = false
    // var holds the current score of the player
    var scoreP: Int = 0
    // var holds value to make sure only 1 coint is collected
    var coinC: Int = -1
    // var hold the amount of times player can pause -1
    var pauseTime: Int = 3
    // var to check if game is paused
    var isItPaused: Boolean = true

    /**
     * This method does everything to be done once the screen is created
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Creates the values to hold the screen width and height
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        // Hide the action bar
        val actionBar = supportActionBar
        actionBar!!.hide()
        // Sets content view
        setContentView(R.layout.minigame1game)
        // Sets all the starting visibilities / coordinates
        setStart(width, height)

        // Creates the draggable motion on the main balloon
        val listener = View.OnTouchListener(function = {view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_MOVE) {

                // Creates values which hold the main balloons x and y coordinates to allow dragging movement
                view.y = motionEvent.rawY - view.height/2 -90
                view.x = motionEvent.rawX - view.width/2

                // Sets the visibilities and detects when the imageViews need to be re-animated
                gameStarted(width, height)

                // Changes the color of the balloon based on the score
                changeBalloon(scoreP % 5, frank)

                // Check for collisions between main balloon and imageViews: spike, spike2, balloon
                collisions(frank, ghost, true, 0)
                collisions(frank, ghost2, true, 0)
                collisions(frank, coin, false, 150)

            } else { // If the player lets go of dragging motion on main balloon
                gamePaused()
            }
            true
        })
        // Declares franks touch listener
        frank.setOnTouchListener(listener)
    }

    /**
     * Checks for collisions between the main balloon and the three imageViews: saw1, saw2, and balloon
     */
    fun collisions(frankImageView: ImageView, imageView: ImageView, death: Boolean, adjuster: Int) {
        val myViewRect = Rect()
        frankImageView.getHitRect(myViewRect)
        // THE FIRST SAW
        val otherViewRect1 = Rect()
        imageView.getHitRect(otherViewRect1)
        otherViewRect1.set(otherViewRect1.left + 170 - adjuster, otherViewRect1.top + 170 - adjuster,
            otherViewRect1.right - 170 + adjuster, otherViewRect1.bottom - 170 + adjuster)

        if (Rect.intersects(myViewRect, otherViewRect1)) {
            if (death) {
                // Play death sound effect
                val resID = resources.getIdentifier("pop", "raw", packageName)
                val mediaPlayer = MediaPlayer.create(this, resID)
                mediaPlayer.start()
                // Update to losing screen and set visibility
                val intent = Intent(this@Minigame1GameActivity, MainActivity::class.java)
                intent.putExtra("score", scoreP.toString())
                startActivity(intent)
                frank.visibility = View.INVISIBLE
                // Saves highscore to SavePreference
                if (scoreP > SavePreference(this@Minigame1GameActivity).getInteger("highscore"))
                    SavePreference(this@Minigame1GameActivity).saveInt("highscore", scoreP)
            } else {
                // Add 1 point to the score
                if (coinC < scoreP) {
                    // Play sound effect for coin
                    val resID = resources.getIdentifier("coin", "raw", packageName)
                    val mediaPlayer = MediaPlayer.create(this, resID)
                    mediaPlayer.start()
                    // Update score counters and set visibility
                    scoreP++
                    coinC = scoreP
                    score.text = getString(R.string.score2lose, scoreP.toString())
                    coin.visibility = View.INVISIBLE
                    changeBalloon((-1..5).random(), coin)
                }
            }
        }
    }

    /**
     * Animate the draggable fire to move in circles
     */
    fun spinBalloon() {
        animator = ValueAnimator.ofFloat(-20f, 20f)
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            // Sets the rotation of the main character
            frank.rotation = value
        }
        // Sets the duration and repeats and starts the main character rotation animation
        animator.duration = 500
        animator.repeatCount = 100
        animator.repeatMode = ValueAnimator.REVERSE
        animator.start()
    }

    /**
     * Changes the balloons to different colors
     */
    fun changeBalloon(chooseNumber: Int, imageView: ImageView) {
        when (chooseNumber) {
            0 -> imageView.setImageResource(R.drawable.one)
            1 -> imageView.setImageResource(R.drawable.three)
            2 -> imageView.setImageResource(R.drawable.two)
            3 -> imageView.setImageResource(R.drawable.four)
            4 -> imageView.setImageResource(R.drawable.five)
            5 -> imageView.setImageResource(R.drawable.six)
            else -> imageView.setImageResource(R.drawable.one)
        }
    }

    /**
     * Animates the spikes and balloon to move downward through the screen for the main balloon
     * to either dodge or collect
     */
    fun animateDown(width: Int, height: Int, imageView: ImageView) {
        animator = ValueAnimator.ofFloat(-ghost.height.toFloat(), height.toFloat() + ghost.height)
        animator.addUpdateListener {
            // Creates value of the translation
            val value = it.animatedValue as Float
            // Sets the value of the translation to the imageView pending on the viewNumber
            imageView.translationY = value
        }
        // Sets the the imageViews x coordinate to a random spot on the screen and
        // changes it to visible pending on which viewNumber
        imageView.x = width.toFloat() / 5f * (0 until 5).random()
        imageView.visibility = View.VISIBLE
        // Creates the duration of the animation and starts it
        if (scoreP <= 15) {
            animator.duration = 4000 / (1 until 4).random().toLong()
        } else if (scoreP <= 30) {
            animator.duration = 3000 / (1 until 4).random().toLong()
        } else {
            animator.duration = 2000 / (1 until 4).random().toLong()
        }
        animator.start()
    }

    /**
     * Animates the saws to spin
     */
    fun spinSaws() {
        animator = ValueAnimator.ofFloat(0f, 3600f)
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            // Sets the rotation of the main balloon
            ghost.rotation = value
            ghost2.rotation = value
        }
        // Sets the duration, the repeat number, and starts the main balloons rotation animation
        animator.duration = 7000
        animator.repeatCount = 1000
        animator.repeatMode = ValueAnimator.REVERSE
        animator.start()
    }

    /**
     * Sets all the visibilities / coordinates / properties in the beginning
     */
    fun setStart(width: Int, height: Int) {
        // Sets the visibilities in the start
        ghost.visibility = View.INVISIBLE
        ghost2.visibility = View.INVISIBLE
        coin.visibility = View.INVISIBLE
        paused.visibility = View.INVISIBLE
        pausedTimer.visibility = View.INVISIBLE

        // Sets the drawable of the balloon
        frank.setImageResource(R.drawable.one)

        // Calls the method to spin the saws
        spinSaws()

        // Sets the x and y coordinates of the views when the game starts
        Handler().postDelayed({
            frank.x = width/2 - frank.width/2.toFloat()
            frank.y = height - frank.height*3.toFloat()
            ghost.x = 500f
            ghost.y = 500f
            pausedTimer.x = width/2 - pausedTimer.width/2.toFloat()
            pausedTimer.y = height/2 - pausedTimer.height/2.toFloat() + pausedTimer.height
            paused.x = width/2 - paused.width/2.toFloat()
            paused.y = height/2 - paused.height/2.toFloat()
            beginText.x = width/2 - beginText.width/2.toFloat()
            beginText.y = height/2 - beginText.height/2.toFloat()
            score.x = width - score.width*1.5.toFloat()
            score.y = score.height/2.toFloat()
        }, 50)
    }

    /**
     * Updates all necessary visibilities and animations when the game begins /
     * main balloon is touched. This occurs concurrently with playtime
     */
    fun gameStarted(width: Int, height: Int) {
        // Sets texts visibility
        beginText.visibility = View.INVISIBLE
        paused.visibility = View.INVISIBLE
        pausedTimer.visibility = View.INVISIBLE
        isItPaused = true

        // Animates the balloon once the user touches the balloon
        if (!balloonTouched) {
            animateDown(width, height, ghost)
            Handler().postDelayed({
                animateDown(width, height, ghost2)
                Handler().postDelayed({
                    animateDown(width, height, coin)
                }, 200)
            }, 200)
            balloonTouched = true
            spinBalloon()
        } else {
            paused.visibility = View.INVISIBLE
        }

        // If coin finishes animation start again
        if (coin.y >= height + coin.height) {
            coinC--
            coin.visibility = View.VISIBLE
            animateDown(width, height, coin)
            changeBalloon((-1..5).random(), coin)
        } // If ghost finishes animation start again
        else if (ghost.y >= height + ghost.height) {
            animateDown(width, height, ghost)
        } // If ghost2 finishes animation start again
        else if (ghost2.y >= height + ghost2.height) {
            animateDown(width, height, ghost2)
        }
    }

    /**
     * Updates all necessary variables / visibilities when player pauses the game
     */
    fun gamePaused() {
        Handler().postDelayed({
            paused.visibility = View.VISIBLE
            pausedTimer.visibility = View.VISIBLE
        }, 500)
        // Updates pause number
        while (isItPaused && balloonTouched) {
            pauseTime--
            isItPaused = false
        }
        when (pauseTime) {
            2 -> pausedTimer.text = getString(R.string.pausedTimer2)
            1 -> pausedTimer.text = getString(R.string.pausedTimer1)
            0 -> pausedTimer.text = getString(R.string.pausedTimer0)
        }
        // If the player pauses more than three times update to losing screen
        if (pauseTime < 0) {
            // Saves highscore to SavePreference
            if (scoreP > SavePreference(this@Minigame1GameActivity).getInteger("highscore"))
                SavePreference(this@Minigame1GameActivity).saveInt("highscore", scoreP)
            val intent = Intent(this@Minigame1GameActivity,MainActivity::class.java)
            intent.putExtra("score",scoreP.toString())
            startActivity(intent)
            frank.visibility = View.INVISIBLE
        }
    }
}