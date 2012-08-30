package com.creatrixelit;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import com.creatrixelit.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

/**
 * 
 * @author Creatrix Elit
 * 
 */
public class MathCrops extends Activity {

	/** sound manager instance */
	private static SoundManager mSoundManager;

	/** Static references */
	private static int screenW, screenH;

	/** The OpenGL View */
	private GLSurfaceView glSurface;

	/** Screen display info */
	private Display display;

	/** Game Level */
	private static int lvl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// sets the Bundle
		super.onCreate(savedInstanceState);

		Bundle bundle = this.getIntent().getExtras();
		if (bundle.getString("param1") != null) {
			lvl = getLevelNum(bundle.getString("param1"));
		}

		// requesting to turn the title OFF
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// making it full screen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Set main.XML as the layout for this Activity
		setContentView(R.layout.main);

		// Here I get the screen info
		display = getWindowManager().getDefaultDisplay();

		// Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity.
		screenW = display.getWidth();
		screenH = display.getHeight();

		glSurface = new GameSurfaceView(this);
		setContentView(glSurface);

		LoadSounds();
	}

	private int getLevelNum(String level) {
		if (level.compareTo("1") == 0)
			return 1;
		else if (level.compareTo("2") == 0)
			return 2;
		else if (level.compareTo("3") == 0)
			return 3;
		else if (level.compareTo("4") == 0)
			return 4;
		else if (level.compareTo("5") == 0)
			return 5;
		else if (level.compareTo("6") == 0)
			return 6;
		else if (level.compareTo("7") == 0)
			return 7;
		else if (level.compareTo("8") == 0)
			return 8;
		else if (level.compareTo("9") == 0)
			return 9;
		else if (level.compareTo("10") == 0)
			return 10;
		else if (level.compareTo("11") == 0)
			return 11;
		else if (level.compareTo("12") == 0)
			return 12;
		else
			return 0;
	}

	private void LoadSounds() {
		mSoundManager = new SoundManager();
		mSoundManager.initSounds(getBaseContext());
		mSoundManager.addSound(1, R.raw.sound); // start game
		mSoundManager.addSound(2, R.raw.beep6); // correct answer
		mSoundManager.addSound(3, R.raw.click); // hint board
		mSoundManager.addSound(4, R.raw.beep1); // wrong answer
		mSoundManager.addSound(5, R.raw.up); // ship goes up
	}

	@Override
	public void onResume() {
		super.onResume();
		glSurface.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		glSurface.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		finish();
	}

	/**
	 * GameView are the GLSurfaceView extensions that handle input and
	 * instantiate the Renderers.
	 * 
	 * @author Creatrix Elit
	 * 
	 */
	public static class GameSurfaceView extends GLSurfaceView {

		private static GameRenderer mRenderer;

		// Touch Input Handlers
		private PointF touchStart;
		private PointF touchMoved;

		/**
		 * Constructor
		 * 
		 * @param context
		 * 
		 * */
		public GameSurfaceView(Context context) {
			super(context);

			// Init input handlers
			initInputHandlers();

			setFocusable(true);

			// Wrapper set so the renderer can
			// access the gl transformation matrixes.
			setGLWrapper(new GLSurfaceView.GLWrapper() {
				@Override
				public GL wrap(GL gl) {
					return new MatrixTrackingGL(gl);
				}
			});

			// set the mRenderer member
			mRenderer = new GameRenderer(context);
			setRenderer(mRenderer);

			// Render the view only when there is a change
			// setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		}

		private void initInputHandlers() {
			touchStart = new PointF();
			touchMoved = new PointF();
		}

		@Override
		public boolean onTouchEvent(MotionEvent e) {
			switch (e.getAction()) {
			case MotionEvent.ACTION_MOVE:
				touchMoved = getDeltaMove(e);
				mRenderer.moveUpdate(touchMoved);

				// requestRender(); // only on dirty mode
				return true;

			case MotionEvent.ACTION_DOWN:
				touchStart = getTouchPoint(e);
				// unproject
				touchStart.set(mRenderer.UnprojectTouchPoint(touchStart));
				mRenderer.touchUpdate(touchStart);

				// requestRender(); // only on dirty mode
				return true;

			case MotionEvent.ACTION_UP:
				// Reset input handlers
				ResetHandlers();

				// touch off screen: hack
				mRenderer.touchUpdate(new PointF(screenW + 1, screenH + 1));

				// requestRender(); // only on dirty mode
				return true;

			default:
				return super.onTouchEvent(e);
			}
		}

		private PointF getTouchPoint(MotionEvent e) {
			return new PointF(e.getX(), e.getY());
		}

		private PointF getDeltaMove(MotionEvent e) {
			return new PointF(e.getX() - touchStart.x, e.getY() - touchStart.y);
		}

		private void ResetHandlers() {
			touchStart = new PointF();
			touchMoved = new PointF();

			// this handles the held down button sound bug
			mRenderer.button_picked_last = -1;
		}
	}

	/**
	 * GameRenderer implements GLSurfaceView Renderer. This is where the game
	 * magic happens.
	 * 
	 * @author Creatrix Elit
	 * 
	 */
	public static class GameRenderer implements GLSurfaceView.Renderer {

		/** Context weak reference */
		private static WeakReference<Context> weakContext;

		/** GL10 */
		private static GL10 gl;

		/** Textures */
		// scene
		private static int Background_Texture;
		private static int Dashboard_Texture;

		// buttons
		private static int ButtonA_Texture;
		private static int ButtonAalt_Texture;
		private static int ButtonB_Texture;
		private static int ButtonBalt_Texture;
		private static int ButtonC_Texture;
		private static int ButtonCalt_Texture;
		private static int ButtonD_Texture;
		private static int ButtonDalt_Texture;
		private static int Hint_Button_Texture, Hint_Button_altTexture;

		// assets
		private static int Cropcircle_Texture_1;
		private static int Cropcircle_Texture_2;
		private static int Cropcircle_Texture_3;
		private static int Cropcircle_Texture_4;
		private static int Cropcircle_Texture_5;
		private static int Ruler_Texture;
		private static int Good_Job_Texture;

		/**
		 * Graphical Assets
		 * */
		private Background background;
		private Element dashboard;
		private DraggableElement ruler;
		private Button Hint_Button, choice_A, choice_B, choice_C, choice_D,
				good_job;

		/**
		 * Game States and other fun stuff
		 */
		private LessonBank lesson;
		private int LESSON_NUM;
		private boolean correct, STALLED, bg_zoomed_in, ship_sound_played_yet,
				play_ship_sound, bg_zoomed_out;
		private float notification_scale = 0, bg_rot = 0.5f;

		/**
		 * Keep track of mElements easily in a Vetor array
		 */
		Vector<Element> mElements;
		private int NUM_OF_ELEMENTS;

		/** Fonts */
		private TextFont mTextFont;
		private int QUESTION_Y, QUESTION_X, ANSWER_Y, ANSWER_X;

		public Unprojector unprojector;

		public GameRenderer(Context context) {
			Log.d("GameRenderer", "Initializing game...");
			init(context);
		}

		private void init(Context context) {
			/*
			 * Init weak reference to context
			 */
			weakContext = new WeakReference<Context>(context);
			unprojector = new Unprojector(-1.0f);
		}

		private void loadTextures() {
			Log.d("loadTextures", "Loading textures...");
			// scene
			Background_Texture = TextureLoader.loadTexture(gl,
					R.drawable.cropfield);
			Dashboard_Texture = TextureLoader.loadTexture(gl, R.drawable.board);

			// buttons
			ButtonA_Texture = TextureLoader.loadTexture(gl, R.drawable.a);
			ButtonAalt_Texture = TextureLoader.loadTexture(gl,
					R.drawable.a_pressed);
			ButtonB_Texture = TextureLoader.loadTexture(gl, R.drawable.b);
			ButtonBalt_Texture = TextureLoader.loadTexture(gl,
					R.drawable.b_pressed);
			ButtonC_Texture = TextureLoader.loadTexture(gl, R.drawable.c);
			ButtonCalt_Texture = TextureLoader.loadTexture(gl,
					R.drawable.c_pressed);
			ButtonD_Texture = TextureLoader.loadTexture(gl, R.drawable.d);
			ButtonDalt_Texture = TextureLoader.loadTexture(gl,
					R.drawable.d_pressed);

			Hint_Button_Texture = TextureLoader.loadTexture(gl, R.drawable.tip);
			Hint_Button_altTexture = TextureLoader.loadTexture(gl,
					R.drawable.tip_bg);

			// assets
			Cropcircle_Texture_1 = TextureLoader.loadTexture(gl,
					R.drawable.crop_circle_1);
			Cropcircle_Texture_2 = TextureLoader.loadTexture(gl,
					R.drawable.crop_circle_2);
			Cropcircle_Texture_3 = TextureLoader.loadTexture(gl,
					R.drawable.crop_circle_3);
			Cropcircle_Texture_4 = TextureLoader.loadTexture(gl,
					R.drawable.crop_circle_4);
			Cropcircle_Texture_5 = TextureLoader.loadTexture(gl,
					R.drawable.crop_circle_5);

			Ruler_Texture = TextureLoader.loadTexture(gl, R.drawable.ruler);

			Good_Job_Texture = TextureLoader.loadTexture(gl,
					R.drawable.nice_job);

		}

		public PointF UnprojectTouchPoint(PointF touchStart) {
			return unprojector.unproject(gl, touchStart);
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			Log.d("onSurfaceCrated", "Configuring...");
			/*
			 * Disable things that aren't needed
			 */
			gl.glDisable(GL10.GL_LIGHTING);
			gl.glDisable(GL10.GL_CULL_FACE);

			/*
			 * Enable Texture Mapping
			 */
			gl.glEnable(GL10.GL_TEXTURE_2D);

			/*
			 * Enable Smooth Shading
			 */
			gl.glShadeModel(GL10.GL_SMOOTH);

			/*
			 * Depth buffer set up
			 */
			gl.glClearDepthf(1.0f);
			gl.glEnable(GL10.GL_DEPTH_TEST);

			/*
			 * The Type Of Depth Testing To Do
			 */
			gl.glDepthFunc(GL10.GL_LEQUAL);

			/*
			 * Really Nice Perspective Calculations
			 */
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

			/*
			 * Initialize game variables
			 */
			initGame(gl);

			mSoundManager.playSound(1);
		}

		private void initGame(GL10 gl) {
			GameRenderer.gl = gl;

			/* Load texture only once */
			loadTextures();

			/* Keep a list of the mElements */
			mElements = new Vector<Element>();

			/* Initialize Global Variables */
			InitializeGlobals();

			/* Populate lesson Bank based on grade level */
			lesson = new LessonBank(gl, lvl); // grade level

			/**
			 * note: Initialization order matters as they are added to the
			 * mElements vector in depth order.
			 */

			/* Set up the game scene */
			InitializeGameScene();

			/* Set up additional game elements */
			InitializeGameElements();

			/* Set up the HUD */
			InitializeGameHUD();

			/* Update the amount of elements in the Elements Vector */
			NUM_OF_ELEMENTS = mElements.size();

			/* Load font file from Assets */
			LoadFonts();
		}

		private void InitializeGlobals() {
			/* Define where in the screen to draw the text */
			QUESTION_Y = (int) (MathCrops.screenH * 3 / 10);
			QUESTION_X = (int) (MathCrops.screenW * 0.85 / 10);

			ANSWER_Y = (int) (MathCrops.screenH * 1.80 / 10);
			ANSWER_X = (int) (MathCrops.screenW * 1.5 / 10);

			/* Initialize lesson number */
			LESSON_NUM = 0;
		}

		private void LoadFonts() {
			Context mContext = weakContext.get();
			if (mContext != null) {
				mTextFont = new TextFont(mContext, gl);
				try {
					mTextFont.LoadFontAlt("verdana.bff", gl);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void InitializeGameScene() {
			/*
			 * Initializing a background
			 */
			background = new Background(gl);

			/*
			 * Dashboard Bacground image
			 */
			dashboard = new Element(gl, 0.7f, 0.2f, // scale
					(float) (screenW * 0.50), // x
					(float) (screenH * 0.78), // y
					-1.03f, // z
					Dashboard_Texture, // texture
					"dashboard", Element.elmType.STATIC); // name

			/**
			 * note: so far, the order in which the elements are initialized
			 * (and therefore drawn) makes all the difference in depth
			 * rendering.
			 */

			float Button_X = screenW * 0.85f;

			float ButtonA_Y = screenH * 0.64f;
			float ButtonB_Y = screenH * 0.72f;
			float ButtonC_Y = screenH * 0.80f;
			float ButtonD_Y = screenH * 0.88f;

			/*
			 * ELement::Button : Answer choice 'A' button
			 */
			choice_A = new Button(gl, 0.1f, 0.025f, Button_X, ButtonA_Y, -1.0f,
					ButtonA_Texture, "A", Button.type.ANSWER_CHOICE_BUTTON,
					ButtonAalt_Texture, Element.elmType.CHOICE_BUTTON);
			// Add elemnt to the list
			mElements.add(choice_A);

			/*
			 * ELement::Button : Answer choice 'B' button
			 */
			choice_B = new Button(gl, 0.1f, 0.025f, Button_X, ButtonB_Y, -1.0f,
					ButtonB_Texture, "B", Button.type.ANSWER_CHOICE_BUTTON,
					ButtonBalt_Texture, Element.elmType.CHOICE_BUTTON);
			// Add elemnt to the list
			mElements.add(choice_B);

			/*
			 * ELement::Button : Answer choice 'C' button
			 */
			choice_C = new Button(gl, 0.1f, 0.025f, Button_X, ButtonC_Y, -1.0f,
					ButtonC_Texture, "C", Button.type.ANSWER_CHOICE_BUTTON,
					ButtonCalt_Texture, Element.elmType.CHOICE_BUTTON);
			// Add elemnt to the list
			mElements.add(choice_C);

			/*
			 * ELement::Button : Answer choice 'D' button
			 */
			choice_D = new Button(gl, 0.1f, 0.025f, Button_X, ButtonD_Y, -1.0f,
					ButtonD_Texture, "D", Button.type.ANSWER_CHOICE_BUTTON,
					ButtonDalt_Texture, Element.elmType.CHOICE_BUTTON);
			// Add elemnt to the list
			mElements.add(choice_D);

			/*
			 * This one is for certain itmes only
			 */
			good_job = new Button(gl, 0.3f, 0.3f, (float) (screenW * 0.5),
					(float) (screenH * 0.5), -1.0f, Good_Job_Texture,
					"good_Job", Button.type.NOTIFICATION, Good_Job_Texture,
					Element.elmType.BUTTON);

			// add this to mElements when appropriate
		}

		private void InitializeGameElements() {
			/*
			 * Element: Ruler
			 */
			ruler = new DraggableElement(gl, 0.16f, 0.1f, // Scale
					screenW * 9 / 10, screenH * 1 / 8, -1f, // (X,Y,Z)
					Ruler_Texture, // Texture
					"Ruler");

			/*
			 * Add element "Ruler" to list of mElements
			 */
			mElements.add(ruler);
		}

		private void InitializeGameHUD() {
			/*
			 * Element::Button : Hint_Button
			 */
			Hint_Button = new Button(gl, 0.1f, 0.05f, // Scale
					screenW * 1 / 10, screenH * 1 / 10, -1.0f, // (X,Y,Z)
					Hint_Button_Texture, // Texture
					"HINT", // Name
					Button.type.HINT_BUTTON, // Button type
					Hint_Button_altTexture, Element.elmType.BUTTON);

			/*
			 * Add element "hint_button" to list of mElements
			 */
			mElements.add(Hint_Button);
		}

		@Override
		public void onDrawFrame(GL10 gl) {
			/*
			 * Clear Screen and Depth Buffer
			 */
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			/*
			 * Reset the Modelview Matrix
			 */
			gl.glLoadIdentity();

			// Firstly, check if the lesson was been implemented
			if ( lesson.NUM_OF_LESSONS != -1 ) {
				/*
				 * 
				 * 
				 * Disable depth test for static objects
				 */
				gl.glDisable(GL10.GL_DEPTH_TEST);
				/*
				 * 
				 * 
				 * Drawing background
				 */
				if (STALLED) {
					play_ship_sound = true;
					AnimateBackgroundZoomOut();
				} else {
					AnimateBackgroundZoomIn();
				}
				background.setDepth(bg_rot);
				background.draw(gl);
				/*
				 * 
				 * 
				 * Drawing the lesson's crop circles if lesson is not stalled
				 */
				if (!STALLED && bg_zoomed_in) {
					lesson.drawObjectsForLesson(gl, LESSON_NUM);
				}
				/*
				 * 
				 * 
				 * Drawingdashboard
				 */
				dashboard.draw(gl);
				/*
				 * 
				 * 
				 * Draing the choice buttons
				 */
				drawDashboardButtons();
				/*
				 * 
				 * 
				 * Ask a question and didplay the answers available. if game isn't
				 * stalled
				 */
				if (!STALLED && bg_zoomed_in) {
					askQuestion();
					displayAnswerChoices();
					/*
					 * 
					 * 
					 * If user is using the ruler draggable object, display the
					 * lesson's measurements information.
					 */
					if (ruler.picked()) {
						if ( isOnTopOfCropcircle(ruler) )
							printLessonInfo();
					}
				}
				/*
				 * 
				 * Enable Depth test again and reset modelview matrix
				 */
				gl.glEnable(GL10.GL_DEPTH_TEST);
				gl.glLoadIdentity();
				/*
				 * 
				 * 
				 * Drawing game elements
				 */
				drawmElements();
				if (!STALLED && bg_zoomed_in) { // When in play
					/*
					 * 
					 * 
					 * If user pressed the 'TIP' button, display the lesson's
					 * tip
					 */
					if (Hint_Button.SCALED_UP) {
						printLessonHint();
					}
					/*
					 * 
					 * 
					 * Checking if answer is correct
					 */
					correct = isCorrect();
					if (correct) {
						// if no more lessons, user wins game
						if (lesson.NUM_OF_LESSONS - 1 == LESSON_NUM) {
							// TODO win
						} else {
							LESSON_NUM++;
						}
						Stall();
					}
				}
				// Stall the application with a notification button
				if (STALLED) {
					// while correct choice taken and notification is not picked
					// if ( bg_zoomed_out )
					NotificationScaleAnimation();
					good_job.setScale(notification_scale);
					good_job.draw(gl);

					if (good_job.picked()) {
						// when picked, reset all
						ship_sound_played_yet = false;
						notification_scale = 0.0f;
						good_job.setPicked(false);
						UnStall();
					}
				}
			}// Lesson not implemented, revert to default notificaiton
			else {
				// TODO
			}
		}

		private boolean isOnTopOfCropcircle(DraggableElement elm) {
			float rx = elm.getPosition().x;
			float ry = elm.getPosition().y;

			if ( rx > screenW/2 - 150 && rx < screenW/2 + 150 ) {
				if ( ry > screenH/2 - 250 && ry < screenH/2 ) {
					return true;
				}
				else return false;
			} else return false;
		}

		/**
		 * @return true if CHOICE_BUTTON picked is the correct answer
		 */
		private int button_picked_last;

		private boolean isCorrect() {
			int i;
			for (i = 0; i < NUM_OF_ELEMENTS; ++i) {
				// Make sure element is a CHOICE_BUTTON
				Element elm = mElements.elementAt(i);
				if (elm.type() == Element.elmType.CHOICE_BUTTON) {
					// Check for picking
					if (elm.picked() && i != button_picked_last) {
						// I don't want the same button held down
						// to be checked at every frame.
						button_picked_last = i;
						// Get the answer for lesson and compare
						String answer = lesson.getAnswer(LESSON_NUM);
						if (answer.compareToIgnoreCase(elm.name) == 0) {
							// play correct sound once
							mSoundManager.playSound(2); // right answer
							return true;
						} else {
							// play wrong sound
							mSoundManager.playSound(4);
							return false;
						}
					}
				}
			}
			return false;
		}

		/****************************
		 * 
		 * Animation Functions
		 ***************************/
		private long zoom_prevtime = 0;

		private void AnimateBackgroundZoomIn() {
			/* ***********************************
			 * 
			 * Zoom in 2 units every 1 second
			 ************************************ */
			long mTime = SystemClock.uptimeMillis();
			long timeDiff = mTime - zoom_prevtime;
			if (timeDiff > 1) {
				// Keep track of time
				zoom_prevtime = mTime;
				
				// 2 units
				bg_rot += 2f;
				
				// (Cap at -40 units of depth)
				if (bg_rot > -40f) {
					bg_rot = -40f;
					
					// Set animation flag
					bg_zoomed_in = true;
				} else {
					// Set animation flag
					bg_zoomed_in = false;
				}
			}
		}

		private void AnimateBackgroundZoomOut() {
			/* ***********************************
			 * 
			 * Play sound only once
			 ************************************ */
			if (play_ship_sound && !ship_sound_played_yet) {
				mSoundManager.playSound(5);
				play_ship_sound = false;
				ship_sound_played_yet = true;
			}
			
			/* ***********************************
			 * 
			 * Zoom out 2 units every 1 second
			 ************************************ */
			long mTime = SystemClock.uptimeMillis();
			long timeDiff = mTime - zoom_prevtime;
			if (timeDiff > 1) {
				// Keep track of time
				zoom_prevtime = mTime;
				
				// Subtract 2 units
				bg_rot -= 2f;
				
				// (Cap at frustum limit)
				if (bg_rot < -100f) {
					bg_rot = -100.0f;
					
					// set animation flag
					bg_zoomed_out = true;
				} else {
					// set animation flag
					bg_zoomed_out = false;
				}
			}
		}

		private long notification_prevtime = 0;

		private void NotificationScaleAnimation() {
			long mTime = SystemClock.uptimeMillis();
			long timeDiff = mTime - notification_prevtime;
			if (timeDiff > 1) {
				notification_prevtime = mTime;
				notification_scale += 0.20;
				if (notification_scale > 1f) {
					notification_scale = 1f;
				}
			}
		}

		/****************************
		 * 
		 * Game State Functions
		 ***************************/
		private void Stall() {
			STALLED = true;
			// temporarily add notification button to list of elements
			mElements.add(good_job);
			NUM_OF_ELEMENTS++;
		}

		private void UnStall() {
			mElements.removeElement(good_job);
			NUM_OF_ELEMENTS--;
			STALLED = false;
		}

		private void drawDashboardButtons() {
			int i;
			for (i = 0; i < NUM_OF_ELEMENTS; ++i) {
				Element elm = mElements.elementAt(i);
				if (elm.type() == Element.elmType.CHOICE_BUTTON)
					elm.draw(gl);
			}
		}

		private void drawmElements() {
			int i;
			for (i = 0; i < NUM_OF_ELEMENTS; ++i) {
				Element elm = mElements.elementAt(i);
				if (elm.type() != Element.elmType.CHOICE_BUTTON)
					elm.draw(gl);
			}
		}

		private void askQuestion() {
			int lineCount = lesson.getQuestionLineCount(LESSON_NUM);
			for (int l = 0; l < lineCount; l++) {
				// Get the line height offset
				int height = mTextFont.GetTextHeight() * l;
				// get the line, and draw it
				drawText(lesson.getQuestionAtLine(LESSON_NUM, l), QUESTION_X,
						QUESTION_Y - height, 0.78f, 1f, 1f, 1f);
			}
		}

		private void displayAnswerChoices() {
			int lineCount = 2;
			for (int l = 0; l < lineCount; l++) {
				// Get the line height offset
				int height = mTextFont.GetTextHeight() * l;
				// get the line, and draw it
				drawText(lesson.getAnswerListAtLine(LESSON_NUM, l), ANSWER_X,
						ANSWER_Y - height, 0.78f, 1.0f, 1.0f, 1f);
			}
		}
		
		private void printLessonInfo() {
			int count = lesson.getLessonInfoCountAt(LESSON_NUM);
			for ( int l=0; l < count; l++) {
				drawText(lesson.getLessonInfoAt(LESSON_NUM, l), 
						screenW * 9/14, screenH * 1/5 + mTextFont.GetTextHeight()*l, 
						0.79f,
						.1f, .0f, .1f);
			}
		}
		
		private void printLessonHint() {
			int count = lesson.getHintLineCount(LESSON_NUM);
			for ( int l=0; l < count; l++) {
				drawText(lesson.getHintAtLine(LESSON_NUM, l), 
						screenW * 1/10, screenH / 2 + mTextFont.GetTextHeight()*l*2, 
						1.2f,
						.1f, .0f, .1f);
			}
		}

		private void drawText(String text, int x, int y, float scale, float r,
				float g, float b) {
			gl.glPushMatrix();
			mTextFont.SetPolyColor(r, g, b);
			mTextFont.SetScale(scale);
			mTextFont.PrintAt(gl, text, x, y);
			gl.glPopMatrix();
		}

		/***********************************
		 * 
		 * Override oGL 'onSurfaceChanged'
		 **********************************/
		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			Log.d("onSurfaceChanged", "Configuring...");

			GameRenderer.gl = gl;

			if (height == 0) { // Prevent A Divide By Zero By
				height = 1; // Making Height Equal One
			}

			/*
			 * Reset the curent viewprt
			 */
			gl.glViewport(0, 0, width, height);

			/*
			 * Select and Reset The Projection Matrix
			 */
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();

			/*
			 * Calculate The Aspect Ratio Of The Window
			 */
			GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
					100.0f);

			/*
			 * Select and Reset The Modelview Matrix
			 */
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
		}

		/**
		 * Update game states based on user input
		 * 
		 * note: I search throughout the elements in reverse order to handle
		 * depth tests the lazy way. :(
		 * 
		 * @param touchMoved
		 */
		public void moveUpdate(PointF touchMoved) {
			int i;
			for (i = NUM_OF_ELEMENTS - 1; i > -1; --i) {
				Element elm = mElements.elementAt(i);

				// If this elements is picked, call on update
				if (elm.picked()) {
					elm.moveUpdate(gl, touchMoved);
					// (If an element is picked, don't check anymore)
					break;
				}
			}
		}

		/**
		 * Check for each element if it is being picked
		 * 
		 * @param touhStart
		 */
		public void touchUpdate(PointF touchStart) {
			int i;
			for (i = 0; i < NUM_OF_ELEMENTS; ++i) {
				Element elm = mElements.elementAt(i);

				// Check for point piking
				elm.setPicked(elm.isPicked(gl, touchStart));

				// If it is indeed picked this time, update element
				if (elm.picked()) {
					elm.touchUpdate(gl, touchStart);
				}
			}
		}

		/**
		 * Quad class
		 * 
		 * @author Creatrix Elit
		 * 
		 */
		public static class Quad {
			// Default vertices
			private float vertices[] = { -140.0f, -100.0f, 0.0f, // 0,
					// Bottom
					// Left
					-140.0f, 100.0f, 0.0f, // 1, Top Left
					140.0f, 100.0f, 0.0f, // 2, Top Right
					140.0f, -100.0f, 0.0f, // 3, Bottom Right
			};

			// The order we like to connect them.
			private short[] indices = { 0, 1, 2, 0, 2, 3 };

			// tex coords
			private float[] uvs = { 0, 0, 0, 1, 1, 1, 1, 0 };

			// Our vertex buffer.
			private FloatBuffer vertexBuffer;

			// Our index buffer.
			private ShortBuffer indexBuffer;

			// Texture buffer
			private FloatBuffer texBuffer;

			public Quad(float w, float h) {
				// Set the size
				Resize(w, h);

				// a float is 4 bytes, therefore we multiply the number if
				// vertices with 4.
				ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
				vbb.order(ByteOrder.nativeOrder());
				vertexBuffer = vbb.asFloatBuffer();
				vertexBuffer.put(vertices);
				vertexBuffer.position(0);

				// short is 2 bytes, therefore we multiply the number if
				// vertices with 2.
				ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
				ibb.order(ByteOrder.nativeOrder());
				indexBuffer = ibb.asShortBuffer();
				indexBuffer.put(indices);
				indexBuffer.position(0);

				// Texture buffer
				ByteBuffer tbb = ByteBuffer.allocateDirect(4 * 2 * 4);
				tbb.order(ByteOrder.nativeOrder());
				texBuffer = tbb.asFloatBuffer();
				texBuffer.put(uvs);
				texBuffer.position(0);

			}

			private void Resize(float w, float h) {
				vertices[0] = -w;
				vertices[1] = -h;
				vertices[2] = 0.0f;

				vertices[3] = -w;
				vertices[4] = h;
				vertices[5] = 0.0f;

				vertices[6] = w;
				vertices[7] = h;
				vertices[8] = 0.0f;

				vertices[9] = w;
				vertices[10] = -h;
				vertices[11] = 0.0f;
			}

			public void draw(GL10 gl) {
				gl.glDisable(GL10.GL_DEPTH_TEST);

				gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
				gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

				// Enable texturing
				gl.glEnable(GL10.GL_TEXTURE_2D);
				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				gl.glActiveTexture(GL10.GL_TEXTURE0);
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuffer);

				gl.glEnable(GL10.GL_BLEND);
				gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

				gl.glDrawElements(GL10.GL_TRIANGLES, indices.length,
						GL10.GL_UNSIGNED_SHORT, indexBuffer);

				// Disable the vertex buffer.
				gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			}

		}

		/**
		 * Background Quad with loaded texture appears as background image.
		 * 
		 * @author Creatrix Elit
		 * 
		 */
		public static class Background {

			private final int height = 50;
			private final int width = 70;
			private float depth = -40f;

			public Background(GL10 gl) {
				plane = new Quad(width, height);
			}

			public void setDepth(float d) {
				depth = d;
			}

			public void draw(GL10 gl) {
				gl.glPushMatrix();
				gl.glBindTexture(GL10.GL_TEXTURE_2D, Background_Texture);
				gl.glTranslatef(0.0f, 0.0f, depth);
				plane.draw(gl);
				gl.glPopMatrix();
			}

			private Quad plane;
		}

		/**
		 * 
		 * This is a class used to instantiate any game element. Elements can be
		 * any type of object in the game, they are simply Quads with the given
		 * dimensions and the given textures.
		 * 
		 * @author Creatrix Elit
		 * 
		 */
		public static class Element {

			// Public
			public String name;

			// Protected
			protected float height;
			protected float width;
			protected PointF touchStart;

			public enum elmType {
				BUTTON, DRAGGABLE, STATIC, CHOICE_BUTTON
			}

			elmType TYPE;

			/**
			 * Update
			 * 
			 * @param touchMoved
			 */
			public void moveUpdate(GL10 gl, PointF touchMoved) {
				// do nothing, let extensions handle this
			}

			public elmType type() {
				return TYPE;
			}

			public void setScale(float s) {
				scale = s;
			}

			public void touchUpdate(GL10 gl, PointF touchStart) {
				this.touchStart = touchStart;
			}

			/**
			 * Element Constructor
			 */
			public Element(GL10 gl, float w, float h, float x, float y,
					float z, int tex, String n, elmType type) {
				//
				this.firstTimeInitialized = true;
				unprojector = new Unprojector(z);
				//
				this.height = h;
				this.width = w;
				this.position = new PointF(x, y);
				this.offset = new PointF(0, 0);
				//
				this.setDepth(z);
				this.setElement(new Quad(width, height));
				//
				this.name = n;
				//
				this.texture = tex;
				//
				this.TYPE = type;

				scale = (1.0f);
			}

			/**
			 * Draw the element
			 */
			public void draw(GL10 gl) {
				gl.glPushMatrix();

				/*
				 * Bind the texture
				 */
				gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
				gl.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);

				/*
				 * If first time this is being drawn, translate to initial
				 * poisition
				 */
				if (firstTimeInitialized) {
					// Set principal location only once
					PointF p = unprojector.unproject(gl, position);
					// offset position with init location
					offset.x += p.x;
					offset.y += p.y;
					firstTimeInitialized = false;
				}

				/*
				 * Translate to given pos
				 */
				gl.glTranslatef(offset.x, offset.y, getDepth());

				/*
				 * Scale
				 */
				gl.glScalef(scale, scale, scale);

				/*
				 * Draw the element
				 */
				getElement().draw(gl);

				gl.glPopMatrix();
			}

			/**
			 * Returns whether this game element was picked
			 * 
			 * @param touch
			 *            - touch event
			 * 
			 * @return if it is picked :: boolean
			 * 
			 */
			public boolean isPicked(GL10 gl, PointF touch) {
				// unproject position
				PointF pos = unprojector.unproject(gl, position);

				if (touch.x > pos.x - width && touch.x < pos.x + width
						&& touch.y > pos.y - height && touch.y < pos.y + height) {
					return true;
				} else
					return false;
			}

			/**
			 * Getters/Setters
			 */
			public void setPosition(PointF move) {
				position.set(move);
			}

			public PointF getPosition() {
				return position;
			}

			public void setOffset(PointF p) {
				offset.set(p);
			}

			public void setPicked(boolean p) {
				picked = p;
			}

			public float getDepth() {
				return depth;
			}

			public boolean picked() {
				return picked;
			}

			public void setDepth(float depth) {
				this.depth = depth;
			}

			public Quad getElement() {
				return element;
			}

			public void setElement(Quad element) {
				this.element = element;
			}

			// Private
			private Unprojector unprojector;
			private float scale;
			private Quad element;
			private PointF position;
			private PointF offset;
			private boolean firstTimeInitialized;
			private float depth;
			private boolean picked;
			protected int texture;
		}

		/**
		 * Buttons extends Element.class. They have the ability to scale if they
		 * are a "hint button" or act accordingly if they are normal buttons
		 * (the only other button types I implemented are the answer key buttons
		 * 'A', 'B', 'C', & 'D'
		 * 
		 * @author Creatrix Elit
		 * 
		 */
		public static class Button extends Element {

			private Unprojector unprojector;

			/**
			 * Math.Crops Game specific Button Action types
			 * 
			 * @author Creatrix Elit
			 */
			public enum type {
				/*
				 * HINT_BUTTON: Displays a hint box which is simply a bigger
				 * button with some hint text that 'destroys' itself when
				 * clicked.
				 */
				HINT_BUTTON,

				/*
				 * ANSWER_CHOICE_BUTTON: Acts as a regular button, and the
				 * action is then handled by the renderer. The only action taken
				 * here is to toggle the button texture.
				 */
				ANSWER_CHOICE_BUTTON,

				/*
				 * 
				 */
				NOTIFICATION
			}

			@Override
			public void touchUpdate(GL10 gl, PointF touch) {
				super.touchUpdate(gl, touch);

				/*
				 * Toggle action corresponding to button type
				 */
				if (picked()) {
					switch (BUTTON_TYPE) {
					case HINT_BUTTON:
						if (HINT_BUTTON_PICKED) {
							hint_sound_played = false;
							HINT_BUTTON_PICKED = false;
						} else
							HINT_BUTTON_PICKED = true;
						break;

					case ANSWER_CHOICE_BUTTON:
						if (CHOICE_BUTTON_PICKED) {
							CHOICE_BUTTON_PICKED = false;
						} else
							CHOICE_BUTTON_PICKED = true;
						break;

					case NOTIFICATION:
						//
						break;
					}
				}// picked
			}

			@Override
			public void draw(GL10 gl) {
				switch (BUTTON_TYPE) {
				case HINT_BUTTON:
					if (HINT_BUTTON_PICKED) {
						// only once
						if (!hint_sound_played) {
							hint_sound_played = true;
							mSoundManager.playSound(3);
						}
						SCALED_UP = false;
						scaleUp(gl);
					} else {
						SCALED_DOWN = false;
						scaleDown(gl);
					}
					break;

				case ANSWER_CHOICE_BUTTON:
					if (CHOICE_BUTTON_PICKED) {
						texture = otherTexture;
						CHOICE_BUTTON_PICKED = false;
					} else {
						texture = orgTexture;
					}
					break;

				case NOTIFICATION:
					//
					break;
				}

				// Draw the Button element
				super.draw(gl);
			}

			/**
			 * scaleUp scales the button up untill it reaches a scale of 1 then
			 * sets the HINT_BUTTON_PICKED flag to false to stop scaling
			 */
			private void scaleUp(GL10 gl) {
				if (!SCALED_UP) {

					// Scale up
					width = 1;
					height = 0.5f;

					// Set new position to the middle of the screen
					setPosition(new PointF(screenW / 2, screenH / 2));

					// project the new position onto the view
					setOffset(unprojector.unproject(gl, getPosition()));
					
					// set the new alternative texture
					texture = otherTexture;

					// set the new element dimensions
					setElement(new Quad(width, height));

					SCALED_UP = true;
				}
			}

			/**
			 * scaleDown does the oposite of the above method
			 */
			private void scaleDown(GL10 gl) {
				if (!SCALED_DOWN) {
					width = orgWidth;
					height = orgHeight;

					// Set new position to teh middle of the screen
					setPosition(orgPos);

					// project the new position onto the view
					setOffset(unprojector.unproject(gl, getPosition()));
					
					// set the new alternative texture
					texture = orgTexture;

					// set the new element dimensions
					setElement(new Quad(width, height));

					SCALED_DOWN = true;
					SCALED_UP = false;
				}
			}

			/**
			 * Button :: Element super constructor
			 * 
			 * @param n
			 *            - The Button's text field
			 * @param altTexture
			 *            -- alterntive texture to display if button picked
			 * 
			 */
			public Button(GL10 gl, float w, float h, float x, float y, float z,
					int tex, String n, type button_type, int altTexture,
					Element.elmType et) {
				super(gl, w, h, x, y, z, tex, n, et);

				this.orgTexture = tex;
				this.orgPos = new PointF(x, y);
				this.otherTexture = altTexture;
				this.orgWidth = w;
				this.orgHeight = h;
				this.BUTTON_TYPE = button_type;

				unprojector = new Unprojector(z);

				SCALED_UP = false;
				SCALED_DOWN = true;
			}

			private boolean hint_sound_played;
			private PointF orgPos;
			private boolean SCALED_UP, SCALED_DOWN;
			private int otherTexture, orgTexture;
			private float orgWidth, orgHeight;
			private type BUTTON_TYPE;
			private boolean HINT_BUTTON_PICKED, CHOICE_BUTTON_PICKED;
		}

		/**
		 * Draggable Elements extend from the simple Element class. These
		 * elements can be dragged around the screen and are projected using the
		 * Unprojector.class into the GLSurfaceView.
		 * 
		 * @author Creatrix Elit
		 * 
		 */
		public class DraggableElement extends Element {

			private Unprojector unprojector;
			private PointF OriginalPos;

			public DraggableElement(GL10 gl, float w, float h, float x,
					float y, float z, int tex, String n) {
				super(gl, w, h, x, y, z, tex, n, Element.elmType.DRAGGABLE);

				OriginalPos = new PointF(x, y);
			}

			@Override
			public void touchUpdate(GL10 gl, PointF touch) {
				super.touchUpdate(gl, touch);
				// Set true if picked at touchStart
				setPicked(isPicked(gl, touch));
			}

			@Override
			public void draw(GL10 gl) {
				if (!picked()) {
					moveUpdate(gl, OriginalPos);
				}

				super.draw(gl);
			}

			/**
			 * Overrides update function in Element
			 */
			@Override
			public void moveUpdate(GL10 gl, PointF touchMoved) {
				super.moveUpdate(gl, touchMoved);

				// Unproject touch move point
				unprojector = new Unprojector(getDepth());

				// Update the element's position
				setOffset(unprojector.unproject(gl, touchMoved));

				// set new position
				setPosition(touchMoved);
			}

		}

		/*********************************************************
		 * 
		 * 
		 * LESSON BANK
		 ********************************************************/
		/**
		 * This will hold all questions along with their corresponding answers
		 * and all of the assets that correspond to the questions for their
		 * corresponding grade level!
		 * 
		 * FUTURE: In the future, I would like to make this as dynamic as
		 * possible. For instance, each lesson has a type associated which can
		 * be generalized enough to simply read from a .xml or .txt file that
		 * the teacher creates and populate new lessons for each grade level.
		 * 
		 */
		public class LessonBank {
			
			// Number of lessons in the grade:
			// (Default is -1 for all unimplemented lessons)
			private int NUM_OF_LESSONS = -1; 
			
			private int	[] 	QuestionLines;  // Line count for each question
			private int	[] 	HintLines;		// Line count for each lesson 'tip'

			/** Q/A String arrays */
			private String	[][] 	Question;	// [question number] [question]
			private String	[] 		Answer;		// [answer to question number]
			private String	[][] 	AnswerList;	// [line][answer string]
			private String	[][] 	Hint;		// [line][hint string]
			private Element	[][] 	Objects;	// [number][Element]
			private int 	[]		ObjectCount;// [object count in lesson number]
			
			private String	[][] 	LessonInfo; // Additional lesson info metrics
			private int		[]		LessonInfoCount;
			
			/**
			 * Constructor
			 * 
			 * @param lvl
			 *            - lesson grade level
			 */
			public LessonBank(GL10 gl, int lvl) {
				populateBank(gl, lvl);
			}

			/**
			 * Draws the objects in current lesson 'lesson'
			 * @param gl
			 * @param i
			 */
			public void drawObjectsForLesson(GL10 gl, int lesson) {
				for (int obj = 0; obj < getObjectCount(lesson); obj++) {
					Objects[lesson][obj].draw(gl);
				}
			}

			/**
			 * Bank Population Methods
			 * 	These are wehre all the lesson's can be created
			 */
			private void TwelfthGrade(GL10 gl) {
				// TODO Auto-generated method stub

			}

			private void EleventhGrade(GL10 gl) {
				// TODO Auto-generated method stub

			}

			private void TenthGrade(GL10 gl) {
				// TODO Auto-generated method stub

			}

			private void NinthGrade(GL10 gl) {
				// TODO Auto-generated method stub

			}

			private void EightGrade(GL10 gl) {
				/* **************************************
				 * 
				 * Initialize variables for lesson
				 ************************************** */
				NUM_OF_LESSONS  = 	2;
				QuestionLines 	= 	new int[NUM_OF_LESSONS];
				HintLines 		= 	new int[NUM_OF_LESSONS];

				Question 	= 	new String[NUM_OF_LESSONS][6];
				Answer 		= 	new String[NUM_OF_LESSONS];
				AnswerList 	= 	new String[NUM_OF_LESSONS][2];
				Hint 		= 	new String[NUM_OF_LESSONS][10];
				Objects 	= 	new Element[NUM_OF_LESSONS][4];
				ObjectCount = 	new int[NUM_OF_LESSONS];
				
				LessonInfo = new String[NUM_OF_LESSONS][4];
				LessonInfoCount = new int[NUM_OF_LESSONS];
				
				/* **************************************
				 * 
				 * 
				 * 
				 * First Lesson
				 ************************************** */

				/* Question strings *
				********************/
				Question[0][0] = " We need to create a triangle the height " +
						"of the symbol";
				Question[0][1] = " (40 meters) which extends 30 meters to " +
						"the left. How";
				Question[0][2] = " long must the hypotenuse be to complete " +
						"our crop circle?";
				
				/* Question line count *
				***********************/
				QuestionLines[0] = 3;
				
				/* Answer Strings  *
				********************/
				AnswerList[0][0] = "A. 30 meters		B. 45 meters";
				AnswerList[0][1] = "C. 900 meters		D. 50 meters";
				
				/* Answer key       *
				 ********************/
				Answer[0] = "D";

				/* Hint strings    *
				********************/
				Hint[0][0] = " Pythagorean Theorem States:";
				Hint[0][1] = " 		A^2 + B^2 = C^2";
				Hint[0][2] = " Where A and B are sides of a triangle, " +
						"and C is the hypotenuse";
				
				/* Hint line count  *
				********************/
				HintLines[0] = 3;

				/* Lesson objects  *
				********************/
				Objects[0][0] = new Element(gl, 0.25f, 0.25f, screenW / 2,
						screenH / 3, -1f, Cropcircle_Texture_1, "crop_circle",
						Element.elmType.STATIC);
				
				/* Lesson object count *
				***********************/
				ObjectCount[0] = 1;
				
				/* Lesson additional information  *
				***********************************/
				LessonInfo[0][0] = "W...40ft";
				LessonInfo[0][1] = "H:...40ft";
				
				/* Lesson info line count  *
				***************************/
				LessonInfoCount[0] = 2;
				

				/* **************************************
				 * 
				 * 
				 * 
				 * Second Lesson
				 * **************************************/

				/* Question strings *
				********************/
				Question[1][0] = " We must place a circle that perfectly " +
						"surrounds this symbol,";
				Question[1][1] = " what is the circumference needed to " +
						"complete the crop circle?";
				
				/* Question line count *
				***********************/
				QuestionLines[1] = 2;
				
				/* Answer Strings  *
				********************/
				AnswerList[1][0] = "A. 90.6 meters		B. 125.6 meters";
				AnswerList[1][1] = "C. 135 meters		D. 150.6 meters";

				/* Answer key       *
				********************/
				Answer[1] = "B";

				/* Hint strings    *
				********************/
				Hint[1][0] = " Circumference of a circle:";
				Hint[1][1] = " 		C = 2 * PI * Radius";
				
				/* Hint line count *
				********************/
				HintLines[1] = 2;

				/* Lesson objects  *
				********************/
				Objects[1][0] = new Element(gl, 0.25f, 0.25f, screenW / 2,
						screenH / 3, -1f, Cropcircle_Texture_2, "crop_circle",
						Element.elmType.STATIC);
				
				/* Lesson object count  *
				*************************/
				ObjectCount[1] = 1;
				
				/* Lesson additional information  *
				***********************************/
				LessonInfo[1][0] = "W:...40ft";
				LessonInfo[1][1] = "H:...40ft";
				
				/* Lesson info line count  *
				***************************/
				LessonInfoCount[1] = 2;
			}

			private void SeventhGrade(GL10 gl) {
				// TODO Auto-generated method stub

			}

			private void SixthGrade(GL10 gl) {
				// TODO Auto-generated method stub

			}

			private void FifthGrade(GL10 gl) {
				// TODO Auto-generated method stub

			}

			private void FourthGrade(GL10 gl) {
				// TODO Auto-generated method stub

			}

			private void ThirdGrade(GL10 gl) {
				// TODO Auto-generated method stub

			}

			private void SecondGrade(GL10 gl) {
				// TODO Auto-generated method stub

			}

			private void FirstGrade(GL10 gl) {
				// TODO Auto-generated method stub

			}
			
			/**
			 * Calls the correct lesson according to the level 'lvl' chosen
			 * @param gl
			 * @param lvl
			 */
			private void populateBank(GL10 gl, int lvl) {
				switch (lvl) {

				case 1:
					FirstGrade(gl);
					break;

				case 2:
					SecondGrade(gl);
					break;

				case 3:
					ThirdGrade(gl);
					break;

				case 4:
					FourthGrade(gl);
					break;

				case 5:
					FifthGrade(gl);
					break;

				case 6:
					SixthGrade(gl);
					break;

				case 7:
					SeventhGrade(gl);
					break;

				case 8:
					EightGrade(gl);
					break;

				case 9:
					NinthGrade(gl);
					break;

				case 10:
					TenthGrade(gl);
					break;

				case 11:
					EleventhGrade(gl);
					break;

				case 12:
					TwelfthGrade(gl);
					break;
				}
			}
			
			/**
			 * Various getter methods
			 */
			public int getObjectCount(int i) {
				return ObjectCount[i];
			}

			public int getNumberOfLessons() {
				return NUM_OF_LESSONS;
			}

			public String getAnswer(int i) {
				return Answer[i];
			}

			public String getAnswerListAtLine(int i, int l) {
				return AnswerList[i][l];
			}

			public int getQuestionLineCount(int i) {
				return QuestionLines[i];
			}

			public String getQuestionAtLine(int i, int l) {
				return Question[i][l];
			}

			public int getHintLineCount(int i) {
				return HintLines[i];
			}

			public String getHintAtLine(int i, int l) {
				return Hint[i][l];
			}
			
			public String getLessonInfoAt(int i, int l) {
				return LessonInfo[i][l];
			}
			
			public int getLessonInfoCountAt(int i) {
				return LessonInfoCount[i];
			}

		}

		/**************************************************************
		 * 
		 * 
		 * Unprojector:
		 * 			Unprojects 2D screen coordinates to 3D oGL world
		 *************************************************************/
		public static class Unprojector {

			private float depth;
			private MatrixGrabber mMatrixGrabber = new MatrixGrabber();
			
			/**
			 * Only information needed is the object's Z position
			 * @param z (depth)
			 */
			public Unprojector(float z) {
				depth = z;
			}

			/**
			 * Point Picking Algorithm:
			 * Unproject 2D touch screen coordinates to 3D world
			 */
			public PointF unproject(GL10 gl, PointF point) {
				/*
				 * Cache 2D touch location (invert y)
				 */
				float x = point.x;
				float y = (int) (screenH - point.y);

				mMatrixGrabber.getCurrentState(gl);
				int[] view = { 0, 0, screenW, screenH };
				float[] pos = new float[4];
				float[] result = null;

				int retval = GLU.gluUnProject(x, y, depth,
						mMatrixGrabber.mModelView, 0,
						mMatrixGrabber.mProjection, 0, view, 0, pos, 0);

				if (retval != GL10.GL_TRUE) {

					Log.e("unproject", GLU.gluErrorString(retval));

				} else {

					result = new float[3];
					result[0] = pos[0] / pos[3];
					result[1] = pos[1] / pos[3];
					result[2] = pos[2] / pos[3];
					result = pos;

				}
				return new PointF(result[0], result[1]);
			}
		}

		/********************************************************************
		 * 
		 * 
		 * Texture loader: 
		 * 	loads textures. (Uses a weak reference to the Activity Context)
		 ********************************************************************/
		public static class TextureLoader {

			private static int c = 0;

			// Get a new texture id:
			private static int newTextureID(GL10 gl) {
				int[] temp = new int[1];
				gl.glDeleteTextures(1, temp, 0);
				gl.glGenTextures(1, temp, 0);
				return temp[0];
			}

			// Will load a texture out of a drawable resource file, and return
			// an OpenGL texture ID:
			public static int loadTexture(GL10 gl, int resource) {

				// In which ID will we be storing this texture?
				int id = newTextureID(gl);

				// We need to flip the textures vertically:
				Matrix flip = new Matrix();
				flip.postScale(1f, -1f);

				// Load up, and flip the texture:
				Context mContext = weakContext.get();
				Bitmap temp = BitmapFactory.decodeResource(
						mContext.getResources(), resource);
				Bitmap bmp = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(),
						temp.getHeight(), flip, true);
				temp.recycle();

				gl.glBindTexture(GL10.GL_TEXTURE_2D, id);

				// Set all of our texture parameters:
				gl.glTexParameterf(GL10.GL_TEXTURE_2D,
						GL10.GL_TEXTURE_MIN_FILTER,
						GL10.GL_LINEAR_MIPMAP_LINEAR);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D,
						GL10.GL_TEXTURE_MAG_FILTER,
						GL10.GL_LINEAR_MIPMAP_LINEAR);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
						GL10.GL_REPEAT);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
						GL10.GL_REPEAT);

				// Generate, and load up all of the mipmaps:
				for (int level = 0, height = bmp.getHeight(), width = bmp
						.getWidth(); true; level++) {
					// Push the bitmap onto the GPU:
					Log.d("TextureLoader", "Pushing bitmap. #" + c++);
					GLUtils.texImage2D(GL10.GL_TEXTURE_2D, level, bmp, 0);

					// We need to stop when the texture is 1x1:
					if (height == 1 && width == 1)
						break;

					// Resize, and let's go again:
					width >>= 1;
					height >>= 1;
					if (width < 1)
						width = 1;
					if (height < 1)
						height = 1;

					Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, width, height,
							true);
					bmp.recycle();
					bmp = bmp2;
				}

				bmp.recycle();

				return id;
			}
		}

	}

	/************************************************************
	 * 
	 * 
	 * Sound Manager
	 ************************************************************/
	public static class SoundManager {

		private SoundPool mSoundPool;
		private HashMap<Integer, Integer> mSoundPoolMap;
		private AudioManager mAudioManager;
		private WeakReference<Context> weakContext;

		public SoundManager() {
		}

		public void initSounds(Context context) {
			weakContext = new WeakReference<Context>(context);
			if (weakContext.get() != null) {
				mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
				mSoundPoolMap = new HashMap<Integer, Integer>();
				mAudioManager = (AudioManager) weakContext.get()
						.getSystemService(Context.AUDIO_SERVICE);
			}
		}

		public void addSound(int Index, int SoundID) {
			mSoundPoolMap.put(Index,
					mSoundPool.load(weakContext.get(), SoundID, 1));
		}

		public void playSound(int index) {

			int streamVolume = mAudioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			mSoundPool.play(mSoundPoolMap.get(index), streamVolume,
					streamVolume, 1, 0, 1f);
		}

		public void playLoopedSound(int index) {

			int streamVolume = mAudioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);

			mSoundPool.play(mSoundPoolMap.get(index), streamVolume,
					streamVolume, 1, 100, 1f);
		}
	}
}
