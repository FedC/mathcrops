package com.creatrixelit;

import com.creatrixelit.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

/**
 * This is the menu class/Activity which instantiates the correct GameActivty.
 * 
 * I am using .xml animations to make a neat slide-menu, similar (although not
 * as polished) to Angry Birds.
 * 
 * @author Creatrix Elit
 *
 */
public class MainMenu extends Activity implements OnTouchListener {

	float downXValue;
	Display display;
	int width, height;
	Point size;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// requesting to turn the title OFF
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// making it full screen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Set main.XML as the layout for this Activity
		setContentView(R.layout.main);

		// get the linear layout form main.xml
		LinearLayout layMain = (LinearLayout) findViewById(R.id.layout_main);
		layMain.setOnTouchListener((OnTouchListener) this);

		// here I get the screen info for various calculations
		display = getWindowManager().getDefaultDisplay();
		width = display.getWidth(); // deprecated
		height = display.getHeight(); // deprecated

		// The buttons and their listeners
		Button grade_1 = (Button) findViewById(R.id.grade_1);
		grade_1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// start grade 1 activity
				StartGame(1);
			}

		});

		Button grade_2 = (Button) findViewById(R.id.grade_2);
		grade_2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// start grade 1 activity
				StartGame(2);
			}

		});

		Button grade_3 = (Button) findViewById(R.id.grade_3);
		grade_3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// start grade 1 activity
				StartGame(3);
			}

		});

	}

	/**
	 * Here is where I call the corresponding game based on the menu entry.
	 *  
	 * @param lvl - grade level
	 */
	private void StartGame(int lvl) {
		Intent launchGame;

		switch (lvl) {
		case 1:
			launchGame = new Intent(this, MathCrops.class);
			startActivity(launchGame);
			break;

		case 2:
			launchGame = new Intent(this, GLRendererActivity.class);
			startActivity(launchGame);
			break;

		case 3:
			launchGame = new Intent(this, MathCrops.class);
			startActivity(launchGame);
			break;

		}
	}

	public boolean onTouch(View arg0, MotionEvent arg1) {

		// Get the action that was done on this touch event
		switch (arg1.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			// store the X value when the user's finger was pressed down
			downXValue = arg1.getX();
			break;
		}

		case MotionEvent.ACTION_UP: {
			// Get the X value when the user released his/her finger
			float currentX = arg1.getX();

			// Get a reference to the ViewFlipper
			ViewFlipper vf = (ViewFlipper) findViewById(R.id.details);

			if (Math.abs(downXValue - currentX) < 2) {
				// user attempted a click, and not a drag
				if (downXValue < width / 2) {
					// Set the animation
					vf.setAnimation(AnimationUtils.loadAnimation(this,
							R.anim.push_left_out));
					// Flip!
					vf.showPrevious();
				} else if (downXValue > width / 2) {
					// Set the animation
					vf.setInAnimation(AnimationUtils.loadAnimation(this,
							R.anim.push_left_in));
					// Flip!
					vf.showNext();
				}

			} else if (Math.abs(downXValue - currentX) > 3) {
				// going backwards: pushing stuff to the right
				if (downXValue < currentX) {

					// Set the animation
					vf.setAnimation(AnimationUtils.loadAnimation(this,
							R.anim.push_left_out));
					// Flip!
					vf.showPrevious();
				}

				// going forwards: pushing stuff to the left
				if (downXValue > currentX) {
					// Get a reference to the ViewFlipper
					// ViewFlipper vf = (ViewFlipper)
					// findViewById(R.id.details);
					// Set the animation
					vf.setInAnimation(AnimationUtils.loadAnimation(this,
							R.anim.push_left_in));
					// Flip!
					vf.showNext();
				}
			}
			break;
		}
		}

		// if you return false, these actions will not be recorded
		return true;
	}
}