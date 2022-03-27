package com.dodgy.balloon

import android.os.Bundle
import android.view.View
import android.os.Handler
import android.content.Intent
import android.media.MediaPlayer
import android.util.DisplayMetrics
import android.animation.ValueAnimator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.minigame1game.*

class MainActivity : AppCompatActivity() {

    // Holds the animator that is reused to animate all the imageViews
    private var animator = ValueAnimator.ofFloat(0f, 0f)!!
    // var to tell if player has already opened app
    var goingToMain: String = ""
    // var holds duration variable for animating main menu text up
    var durationForIntro = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Creates the values to hold the screen width and height
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        // Sets the view to activity_main and hides action bar
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.hide()

        // var holds the score when player lost
        var score = intent.getStringExtra("score")
        score2lose.text = getString(R.string.score2lose, score)
        var highscoreString: String = SavePreference(this@MainActivity).getInteger("highscore").toString()
        highscore.text = getString(R.string.highscore, highscoreString)

        if (intent.getStringExtra("goingToMain") != null) {
            goingToMain = "true"
        }

        // Sets the visibilities for the imageViews once the application starts
        if (score == null)
            beginVis("null")
        else
            beginVis(score)

        // Animates all the clouds to move into the screen
        animateClouds(width, height)

        // Actions if the player is going to the main menu
        if (score == null) {
            comingFromMain(width, height)
        } else { // Actions if the player lost the game
            lostGame(width, height)
        }

        // Sends View to Minigame1GameActivity when start is clicked
        goToMini1game.setOnClickListener {
            goToMini(goToMini1game, height.toFloat())
        }

        // Sends View to Minigame1GameActivity when retry is clicked
        retry.setOnClickListener {
            goToMini(retry, height.toFloat())
        }

        // Sends View to Minigame1GameActivity when main menu is clicked
        mainMenu.setOnClickListener {
            // Plays pop sound effect
            val mediaPlayer3 = MediaPlayer.create(this, R.raw.pop)
            mediaPlayer3.start()

            //  Lifts all clouds and title text up off the screen
            moveEverythingUp(height*1.2.toFloat())

            // Turns the start button invisible to make it seem like it popped
            mainMenu.visibility = View.INVISIBLE

            // Sets the score back to null
            score = null

            // sets goingToMain to true
            goingToMain = "true"

            // Moves the screen to gameplay screen
            Handler().postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                val intent2 = Intent(this@MainActivity,MainActivity::class.java)
                intent2.putExtra("goingToMain",goingToMain)
                startActivity(intent2)
            }, 2500)
        }
    }

    /**
     * Lifts all clouds and title text up off the screen
     */
    fun moveAllUp(imageViewY: Float, imageView: ImageView, height: Float) {
        animator = ValueAnimator.ofFloat(imageViewY, -height)
        animator.addUpdateListener {
            // Creates value of the translation
            val value = it.animatedValue as Float
            // Sets the value of the translation
            imageView.translationY = value
        }
        // Creates the duration of the animation and starts it
        animator.duration = 1000
        animator.start()
    }

    /**
     * Animate the the text to fly upwards to begin the opening animation of the app
     */
    fun animateTextUp(startingY: Float, endingY: Float, imageView: ImageView, duration: Int) {
        animator = ValueAnimator.ofFloat(startingY*2, endingY)
        animator.addUpdateListener {
            // Creates value of the translation
            val value1 = it.animatedValue as Float
            // Sets the value of the translation
            imageView.translationY = value1
        }
        // Creates the duration of the animation and starts it
        animator.duration = duration.toLong()
        animator.start()
    }

    /**
     * Animate the title to sway sideways
     */
    fun animateTitleSway(imageViewX: Float, imageView: ImageView) {
        animator = ValueAnimator.ofFloat(imageViewX - 15f, imageViewX + 15f)
        animator.addUpdateListener {
            // Creates value of the translation
            val value = it.animatedValue as Float
            // Sets the value of the translation
            imageView.translationX = value
        }
        // Creates the duration of the animation and starts it
        animator.duration = 2400
        animator.repeatCount = 100
        animator.repeatMode = ValueAnimator.REVERSE
        animator.start()
    }

    /**
     * Animate the clouds to move through the screen
     */
    fun animateCloud(startingX: Float, endingX: Float, imageView: ImageView) {
        animator = ValueAnimator.ofFloat(startingX, endingX)
        animator.addUpdateListener {
            // Creates value of the translation
            val value = it.animatedValue as Float
            // Sets the value of the translation
            imageView.translationX = value
            imageView.visibility = View.VISIBLE
        }
        // Creates the duration of the animation and starts it
        animator.duration = 15000
        animator.repeatCount = 100
        animator.start()
    }

    /**
     * Calls method to move all imageViews up
     */
    fun moveEverythingUp(height: Float) {
        // Lifts all clouds and title text up off the screen
        moveAllUp(cloud1.y/5, cloud1, height)
        moveAllUp(cloud2.y/3, cloud2, height)
        moveAllUp(cloud3.y/3, cloud3, height)
        moveAllUp(cloud4.y/3, cloud4, height)
        moveAllUp(youlost.y, youlost, height)
        moveAllUp(retry.y, retry, height)
        moveAllUp(mainMenu.y, mainMenu, height)
        moveAllUp(dodgyText.y, dodgyText, height)
    }

    /**
     *
     */
    fun animateClouds(width: Int, height: Int) {
        Handler().postDelayed({
            // Sets the y values for the clouds
            cloud1.y = 0f
            cloud3.y = height/2.5.toFloat()
            cloud4.y = height/2.toFloat() + cloud4.height/2
            cloud2.y = cloud2.height/2.toFloat()
            // sets the x and y coordinates of the score
            score2lose.x = width/2 - score2lose.width/2.toFloat()
            score2lose.y = height/2 - score2lose.height.toFloat()
            highscore.x = width/2 - highscore.width/2.toFloat()
            highscore.y = height/2 - highscore.height*2.toFloat()

            // Animates each cloud at different intervals
            animateCloud(width + cloud1.width.toFloat(), -cloud1.width*1.4.toFloat(), cloud1)
            Handler().postDelayed({
                animateCloud(width + cloud3.width.toFloat(), -cloud3.width*1.3.toFloat(), cloud3)
            }, 9000)
            Handler().postDelayed({
                animateCloud(-cloud4.width*1.3.toFloat(), width + cloud4.width.toFloat(), cloud4)
            }, 100)
            Handler().postDelayed({
                animateCloud(-cloud2.width*1.4.toFloat(), width + cloud2.width.toFloat(), cloud2)
            }, 6000)
        }, 100)
    }

    /**
     *
     */
    fun comingFromMain(width: Int, height: Int) {
        Handler().postDelayed({
            if (goingToMain != "") {
                gamey.visibility = View.INVISIBLE
                durationForIntro = 5
            }

            // Starts the soundtrack for the game on repeat once the app starts
            Handler().postDelayed({
                if (goingToMain == "") {
                    var mp = MediaPlayer.create(this, R.raw.sound_track)
                    mp.isLooping = true
                    mp.start()
                    gamey.visibility = View.INVISIBLE

//                    val mediaPlayerSound = MediaPlayer.create(this, R.raw.sound_track)
//                    mediaPlayerSound.start()
                }
            }, 2000)

            // Animates the text on screen to fly up into place
            animateTextUp(height + dodgyText.height.toFloat(), dodgyText.height.toFloat()/4, dodgyText, 6000 / durationForIntro)
            animateTextUp(height + goToMini1game.height.toFloat(), height - goToMini1game.height.toFloat()*2, goToMini1game, 6000 / durationForIntro)

            // Sways the text on screen back and fourth slowly
            animateTitleSway(width/2 - dodgyText.width/2.toFloat(), dodgyText)
            animateTitleSway(width/2 - goToMini1game.width/2.toFloat(), goToMini1game)

            // Sets the visibilities for the main menu
            youlost.visibility = View.INVISIBLE
            mainMenu.visibility = View.INVISIBLE
            retry.visibility = View.INVISIBLE
            dodgyText.visibility = View.VISIBLE
            goToMini1game.visibility = View.VISIBLE
        }, 100)
    }

    fun beginVis(score: String) {
        // Sets visibilities of the cloud for the beginning
        cloud1.visibility = View.INVISIBLE
        cloud2.visibility = View.INVISIBLE
        cloud3.visibility = View.INVISIBLE
        cloud4.visibility = View.INVISIBLE
        // If the score is null it will not be visible, if its not null,
        // the score will become visible
        if (score == "null") {
            score2lose.visibility = View.INVISIBLE
            highscore.visibility = View.INVISIBLE
        } else {
            score2lose.visibility = View.INVISIBLE
            highscore.visibility = View.INVISIBLE
            Handler().postDelayed({
                score2lose.visibility = View.VISIBLE
                highscore.visibility = View.VISIBLE
            }, 2000)
        }
    }

    /**
     *
     */
    fun lostGame(width: Int, height: Int) {
        Handler().postDelayed({

            gamey.visibility = View.INVISIBLE

            // Animates the text on screen to fly up into place
            animateTextUp(height + youlost.height.toFloat(), youlost.height.toFloat(), youlost, 2000)
            animateTextUp(height + mainMenu.height.toFloat(), height - mainMenu.height.toFloat()*3, mainMenu, 2000)
            animateTextUp(height + retry.height.toFloat(), height - retry.height.toFloat()*2, retry, 2000)

            // Sways the text on screen back and fourth slowly
            animateTitleSway(width/2 - youlost.width/2.toFloat(), youlost)
            animateTitleSway(width/2 - mainMenu.width/2.toFloat(), mainMenu)
            animateTitleSway(width/2 - retry.width/2.toFloat(), retry)

            // Sets the visibilities for the lose screen
            youlost.visibility = View.VISIBLE
            mainMenu.visibility = View.VISIBLE
            retry.visibility = View.VISIBLE
            dodgyText.visibility = View.INVISIBLE
            goToMini1game.visibility = View.INVISIBLE
        }, 100)
    }

    /**
     *
     */
    fun goToMini(imageView: ImageView, height: Float) {
        // Plays pop sound effect
        val mediaPlayer3 = MediaPlayer.create(this, R.raw.pop)
        mediaPlayer3.start()

        // Lifts all clouds and title text up off the screen
        moveEverythingUp(height*1.2.toFloat())

        // Turns the start button invisible to make it seem like it popped
        imageView.visibility = View.INVISIBLE

        // Moves the screen to gameplay screen
        Handler().postDelayed({
            startActivity(Intent(this, Minigame1GameActivity::class.java))
        }, 2500)
    }
}