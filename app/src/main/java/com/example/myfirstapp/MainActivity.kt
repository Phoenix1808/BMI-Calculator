package com.example.myfirstapp
import android.animation.ValueAnimator
import android.os.Bundle

import android.speech.tts.TextToSpeech
import java.util.Locale
import android.widget.Toast
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.slider.Slider


class MainActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
private lateinit var maleButton: AppCompatButton
private lateinit var femaleButton: AppCompatButton
private lateinit var otherButton: AppCompatButton
private lateinit var lottieAnimation: LottieAnimationView
private lateinit var backgroundAnimation: LottieAnimationView
private var selectedGender: String = "Male"

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    textToSpeech = TextToSpeech(this) { status ->
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.US
        } else {
            Toast.makeText(this, "Text-to-Speech initialization failed!", Toast.LENGTH_SHORT).show()
        }
    }

    val weightValue = findViewById<TextView>(R.id.weightValue)
    val weightSlider = findViewById<Slider>(R.id.weightSlider)
    weightSlider.addOnChangeListener { _, value, _ ->
        weightValue.text = value.toInt().toString()
    }
    val heightValue = findViewById<TextView>(R.id.heightValue)
    val heightSlider = findViewById<Slider>(R.id.heightSlider)
    heightSlider.addOnChangeListener { _, value, _ ->
        heightValue.text = value.toInt().toString()
    }
    val button = findViewById<AppCompatButton>(R.id.calcButton)
    val answerText = findViewById<TextView>(R.id.answer)
    lottieAnimation = findViewById(R.id.bmiAnimation) // Initialize Lottie view
    backgroundAnimation = findViewById(R.id.bmiBackgroundAnimation)

    backgroundAnimation.apply {
        setAnimation(R.raw.heartbeat)
        repeatCount = ValueAnimator.INFINITE
        playAnimation()
    }

    maleButton = findViewById(R.id.maleButton)
    femaleButton = findViewById(R.id.femaleButton)
    otherButton = findViewById(R.id.otherButton)

        maleButton.setOnClickListener { selectGender(maleButton, "Male") }
        femaleButton.setOnClickListener { selectGender(femaleButton, "Female") }
        otherButton.setOnClickListener { selectGender(otherButton, "Other") }

        button.setOnClickListener {
            val weight = weightSlider.value
            val height = heightSlider.value

            // Validate input
            if (weight > 0 && height > 0) {
                val heightInMeters = height / 100
                val bmi = weight / (heightInMeters * heightInMeters)
                val bmiResult = String.format("%.2f", bmi)

                val (bmiCategory,animationResource) = when {
                    bmi < 18.5 -> "Underweight" to R.raw.sad
                    bmi < 25 -> "Normal Weight" to R.raw.happy
                    bmi < 30 -> "Overweight"    to R.raw.thinking
                    else -> "Obese"             to R.raw.sad
                }
                // Update the answer TextView
                answerText.text = "Gender: $selectedGender \nBMI: $bmiResult\nRemarks: $bmiCategory"
               val healthTip = getHealthTip(bmiCategory)

                val healthTipTextView = findViewById<TextView>(R.id.healthTip)
                healthTipTextView.text = healthTip
                healthTipTextView.visibility = View.VISIBLE


                applyTextViewAnimation(answerText)
                applyTextViewAnimation(healthTipTextView)
                playLottieAnimation(animationResource)


                showLottieAnimation()
                val ttsMessage = "Your BMI is $bmiResult,which is considered $bmiCategory.Here is a health tip: $healthTip "
                speakText(ttsMessage)
            } else {
                answerText.text = "Invalid Input"
                applyTextViewAnimation(answerText)
            }
        }
    }
    private fun getHealthTip(bmiCategory: String): String {
        return when (bmiCategory) {
            "Underweight" -> "Consider a balanced diet with more calories."
            "Normal Weight" -> "Great job! Keep maintaining a healthy lifestyle."
            "Overweight" -> "Incorporate regular exercise into your routine."
            "Obese" -> "Consult a nutritionist for a tailored health plan."
            else -> "Maintain a healthy lifestyle for overall wellness."
        }
    }


    private fun selectGender(button: AppCompatButton, gender: String) {

        maleButton.setBackgroundResource(R.drawable.gender_button)
        femaleButton.setBackgroundResource(R.drawable.gender_button)
        otherButton.setBackgroundResource(R.drawable.gender_button)


        button.setBackgroundResource(R.drawable.gender_button_selected)


        selectedGender = gender
    }

    private fun applyTextViewAnimation(textView: TextView) {
        val fadeIn = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 1000
            fillAfter = true
        }
        textView.startAnimation(fadeIn)
    }

    private fun speakText(text: String) {
        if (::textToSpeech.isInitialized) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Toast.makeText(this, "Text-to-Speech is not ready yet.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {

        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }

    private fun animateSlider(slider: Slider, targetValue: Float) {
        val animator = ValueAnimator.ofFloat(slider.value, targetValue).apply {
            duration = 1000
            addUpdateListener { animation ->
                slider.value = animation.animatedValue as Float
            }
        }
        animator.start()
    }
    private fun showLottieAnimation() {
        lottieAnimation.apply {
            visibility = View.VISIBLE
            playAnimation()

            postDelayed({
                visibility=View.GONE
            },5000)
        }
    }
    private fun playLottieAnimation(animationResource: Int) {
        lottieAnimation.apply {
            setAnimation(animationResource)
            visibility = View.VISIBLE
            playAnimation()

            postDelayed({
                visibility = View.GONE
            }, 2000)
        }
    }
}
