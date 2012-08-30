package com.creatrixelit;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import com.creatrixelit.R;
import com.creatrixelit.R.drawable;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class MathCrops extends Activity {

	/** Static references */
	private static int screenW, screenH;

	/** The OpenGL View */
	private GLSurfaceView glSurface;

	/** Screen display info */
	private Display display;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// sets the Bundle
		super.onCreate(savedInstanceState);

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
		 *            view
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
			setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
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

				requestRender();
				return true;

			case MotionEvent.ACTION_DOWN:
				touchStart = getTouchPoint(e);
				// unproject
				touchStart.set(mRenderer.UnprojectTouchPoint(touchStart));
				mRenderer.touchUpdate(touchStart);

				requestRender();
				return true;

			case MotionEvent.ACTION_UP:
				touchStart.set(0f, 0f);
				touchMoved.set(0f, 0f);
				mRenderer.touchUpdate(touchStart);

				requestRender();
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

		private GL10 gl;

		/**
		 * Graphical Assets
		 * */
		private Background background;
		private Element dashboard;
		private DraggableElement avatar;
		private Button Hint_Button, choice_A, choice_B, choice_C, choice_D;

		/**
		 * Game Assets
		 */
		private LessonBank lesson;
		private int LESSON_NUM;

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
			init(context);
		}

		private void init(Context context) {
			/*
			 * Init weak reference to context
			 */
			weakContext = new WeakReference<Context>(context);
			unprojector = new Unprojector(-1.0f);
		}

		public PointF UnprojectTouchPoint(PointF touchStart) {
			return unprojector.unproject(gl, touchStart);
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
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
		}

		private void initGame(GL10 gl) {
			this.gl = gl;

			/*  Keep a list of the mElements */
			mElements = new Vector<Element>();

			/* Initialize Global Variables */
			InitializeGlobals();

			/* Populate lesson Bank based on grade level */
			lesson = new LessonBank(gl, 8); // grade level

			/** TODO
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
			/* Define where in the screen to draw the text  */
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
			background = new Background(gl, R.drawable.cropfield);

			/*
			 * Dashboard Bacground image
			 */
			dashboard = new Element(gl, 0.7f, 0.2f, // scale
					(float) (screenW * 0.50), // x
					(float) (screenH * 0.78), // y
					-1.05f, // z
					R.drawable.board, // texture
					"dashboard"); // name

			/** TODO
			 * note: so far, the order in which the elements are initialized
			 * (and therefore drawn) makes all the difference in depth
			 * rendering.
			 */

			/*
			 * ELement::Button : Answer choice 'A' button
			 */
			choice_A = new Button(gl, 0.05f, 0.05f, (float) (screenW * 0.80),
					(float) (screenH * 0.75), -1.0f, R.drawable.a, "choice_A",
					Button.type.ANSWER_CHOICE_BUTTON, R.drawable.a);// TODO
																	// pressed
																	// texture
			// Add elemnt to the list
			mElements.add(choice_A);

			/*
			 * ELement::Button : Answer choice 'B' button
			 */
			choice_B = new Button(gl, 0.05f, 0.05f, (float) (screenW * 0.85),
					(float) (screenH * 0.65), -1.0f, R.drawable.b, "choice_A",
					Button.type.ANSWER_CHOICE_BUTTON, R.drawable.b);// TODO
																	// pressed
																	// texture
			// Add elemnt to the list
			mElements.add(choice_B);

			/*
			 * ELement::Button : Answer choice 'C' button
			 */
			choice_C = new Button(gl, 0.05f, 0.05f, (float) (screenW * 0.90),
					(float) (screenH * 0.75), -1.0f, R.drawable.c, "choice_A",
					Button.type.ANSWER_CHOICE_BUTTON, R.drawable.c);// TODO
																	// pressed
																	// texture
			// Add elemnt to the list
			mElements.add(choice_C);

			/*
			 * ELement::Button : Answer choice 'D' button
			 */
			choice_D = new Button(gl, 0.05f, 0.05f, (float) (screenW * 0.85),
					(float) (screenH * 0.85), -1.0f, R.drawable.d, "choice_A",
					Button.type.ANSWER_CHOICE_BUTTON, R.drawable.d);// TODO
																	// pressed
																	// texture
			// Add elemnt to the list
			mElements.add(choice_D);

		}

		private void InitializeGameElements() {
			/*
			 * Element: Avatar
			 */
			avatar = new DraggableElement(gl, 0.1f, 0.1f, // Scale
					screenW / 2, 100f, -1.0f, // (X,Y,Z)
					R.drawable.d, // Texture
					"Avatar");

			/*
			 * Add element "Avatar" to list of mElements
			 */
			mElements.add(avatar);
		}

		private void InitializeGameHUD() {
			/*
			 * Element::Button : Hint_Button
			 */
			Hint_Button = new Button(gl, 0.1f, 0.05f, // Scale
					screenW * 1 / 10, screenH * 1 / 10, -1.0f, // (X,Y,Z)
					R.drawable.tip, // Texture
					"HINT", // Name
					Button.type.HINT_BUTTON, // Button type
					R.drawable.tip_bg);

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

			/*
			 * Disable depth test for static objects
			 */
			gl.glDisable(GL10.GL_DEPTH_TEST);

			/*
			 * Drawing background
			 */
			background.draw(gl);

			/*
			 * Drawingdashboard
			 */
			dashboard.draw(gl);

			// TODO if game is started, show the first lesson.
			// if user answered question correctly, show the next question.

			/*
			 * Ask a question and didplay the answers available.
			 */
			askQuestion();
			displayAnswerChoices();

			/*
			 * Enable Depth test again and reset modelview matrix
			 */
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glLoadIdentity();

			/*
			 * Drawing game elements
			 */
			drawmElements();
		}

		private void drawmElements() {
			int i;
			for (i = 0; i < NUM_OF_ELEMENTS; ++i) {
				mElements.elementAt(i).draw(gl);
			}
		}

		private void askQuestion() {
			int lineCount = lesson.getQuestionLineCount(LESSON_NUM);
			for (int l = 0; l < lineCount; l++) {
				// Get the line height offset
				int height = mTextFont.GetTextHeight() * l;
				// get the line, and draw it
				drawText(lesson.getQuestionAtLine(LESSON_NUM, l), QUESTION_X,
						QUESTION_Y - height, 0.75f, 1f, 1f, 1f);
			}
		}

		private void displayAnswerChoices() {
			int lineCount = 2;
			for (int l = 0; l < lineCount; l++) {
				// Get the line height offset
				int height = mTextFont.GetTextHeight() * l;
				// get the line, and draw it
				drawText(lesson.getAnswerListAtLine(LESSON_NUM, l), ANSWER_X,
						ANSWER_Y - height, 0.75f, 1.0f, 1.0f, 1f);
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

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			this.gl = gl;

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
		 * TODO: I search throughout the elements in reverse order to handle
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
				mElements.elementAt(i).touchUpdate(gl, touchStart);
			}
		}

		public static class TextureLoader {

			// Get a new texture id:
			private static int newTextureID(GL10 gl) {
				int[] temp = new int[1];
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

			public Background(GL10 gl, int tex) {
				plane = new Quad(width, height);
				texture = TextureLoader.loadTexture(gl, tex);
			}

			public void draw(GL10 gl) {
				gl.glPushMatrix();
				gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
				gl.glTranslatef(0.0f, 0.0f, -100f);
				plane.draw(gl);
				gl.glPopMatrix();
			}

			private Quad plane;
			private int texture;
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

			private Unprojector unprojector;

			/**
			 * Update
			 * 
			 * @param touchMoved
			 */
			public void moveUpdate(GL10 gl, PointF touchMoved) {
				// do nothing, let extensions handle this
			}

			public void touchUpdate(GL10 gl, PointF touchStart) {
				this.touchStart = touchStart;
			}

			/**
			 * Draw the element
			 */
			public void draw(GL10 gl) {
				gl.glPushMatrix();

				/*
				 * Biind the texture
				 */
				gl.glBindTexture(GL10.GL_TEXTURE_2D, getTexture());
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
			 * @param pos
			 *            - Must be unprojected position for it to work
			 * 
			 * @return if it is picked, it returns the depth of the object
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

			public int getTexture() {
				return texture;
			}

			public void setTexture(int texture) {
				this.texture = texture;
			}

			public Quad getElement() {
				return element;
			}

			public void setElement(Quad element) {
				this.element = element;
			}

			/**
			 * Element Constructor
			 */
			public Element(GL10 gl, float w, float h, float x, float y,
					float z, int tex, String n) {
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
				this.setTexture(TextureLoader.loadTexture(gl, tex));
				//
				this.name = n;
			}

			/**
			 * Global Variables
			 */
			// Public
			public String name;

			// Protected
			protected float height;
			protected float width;
			protected PointF touchStart;

			// Private
			private Quad element;
			private PointF position;
			private PointF offset;
			private boolean firstTimeInitialized;
			private float depth;
			private boolean picked;
			private int texture;
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

			}

			@Override
			public void touchUpdate(GL10 gl, PointF touch) {
				super.touchUpdate(gl, touch);

				// Set true if picked at touchStart
				setPicked(isPicked(gl, touch));

				/*
				 * Toggle action corresponding to button type
				 */
				if (picked()) {
					switch (BUTTON_TYPE) {
					case HINT_BUTTON:
						if (HINT_BUTTON_PICKED)
							HINT_BUTTON_PICKED = false;
						else
							HINT_BUTTON_PICKED = true;
						break;

					case ANSWER_CHOICE_BUTTON:
						if (CHOICE_BUTTON_PICKED)
							CHOICE_BUTTON_PICKED = false;
						else
							CHOICE_BUTTON_PICKED = true;
						break;
					}
				}// picked
			}

			@Override
			public void draw(GL10 gl) {
				/*
				 * Hint Button scales up or down
				 */
				if (HINT_BUTTON_PICKED) {
					SCALED_UP = false;
					scaleUp(gl);
				} else {
					SCALED_DOWN = false;
					scaleDown(gl);
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
					setTexture(TextureLoader.loadTexture(gl, otherTexture));

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
					setTexture(TextureLoader.loadTexture(gl, orgTexture));

					// set the new element dimensions
					setElement(new Quad(width, height));

					SCALED_DOWN = true;
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
					int tex, String n, type button_type, int altTexture) {
				super(gl, w, h, x, y, z, tex, n);

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

			public DraggableElement(GL10 gl, float w, float h, float x,
					float y, float z, int tex, String n) {
				super(gl, w, h, x, y, z, tex, n);
			}

			@Override
			public void touchUpdate(GL10 gl, PointF touch) {
				super.touchUpdate(gl, touch);

				// Set true if picked at touchStart
				setPicked(isPicked(gl, touch));
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

		/**
		 * Oh, yes... the Lesson Bank. This will hold all questions along with
		 * their corresponding answers and all of the assets that correspond to
		 * the questions for their corresponding grade level!
		 * 
		 * TODO: In the future, I would like to make this as dynamic as
		 * possible. For instance, each lesson has a type associated which can
		 * be generalized enough to simply read from a .xml or .txt file that
		 * the teacher creates and populate new lessons for each grade level.
		 * 
		 * @author Creatrix Elit
		 * 
		 */
		public class LessonBank {

			/**
			 * Constructor
			 * 
			 * @param lvl
			 *            - lesson grade level
			 */
			public LessonBank(GL10 gl, int lvl) {
				populateBank(gl, lvl);
			}

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
			 * Various functionality & get/set methods
			 */
			public int getNumberOfLessons() {
				/*
				 * TODO: depending on the number of lessons for each grade level
				 * present this value should be different.
				 */
				return NUM_OF_LESSONS;
			}

			public char getAnswer(int i) {
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

			private int getObjectCount(int i) {
				return ObjectCount[i];
			}

			public void drawObjectsForLesson(GL10 gl, int i) {
				for (int j = 0; j < getObjectCount(i); j++) {
					Objects[i][j].draw(gl);
				}
			}

			/**
			 * Bank Population Methods
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
				/**
				 * TODO: It would be ideal to populate these from external
				 * files.
				 */

				/*
				 * First Lesson
				 */
				Question[0][0] = " We need to create a triangle the height of the symbol";
				Question[0][1] = " (40 meters) which extends 30 meters to the left. How";
				Question[0][2] = " long must the hypotenuse be to complete our crop circle?";
				QuestionLines[0] = 3;

				AnswerList[0][0] = "A. 30 meters		B. 45 meters";
				AnswerList[0][1] = "C. 900 meters		D. 50 meters";
				Answer[0] = 'D';

				Hint[0][0] = " Pythagorean Theorem States:";
				Hint[0][1] = " 		A^2 + B^2 = C^2";
				Hint[0][2] = " Where A and B are sides of a triangle, and C is the hypotenuse";
				HintLines[0] = 3;

				Objects[0][0] = new Element(gl, 0.1f, 0.1f, screenW / 2,
						screenH / 2, -1f, drawable.crop_circle_1,
						"crop_circle_1");
				ObjectCount[0] = 1;

				/*
				 * Second Lesson
				 */
				Question[1][0] = " We must place a circle that perfectly surrounds this symbol,";
				Question[1][1] = " what is the circumference needed to complete the crop circle?";
				QuestionLines[1] = 2;

				AnswerList[1][0] = "A. 90.6 meters		B. 125.6 meters";
				AnswerList[1][1] = "C. 135 meters		D. 150.6 meters";

				Answer[1] = 'B';

				Hint[1][0] = " Circumference of a circle:";
				Hint[1][1] = " 		C = 2 * PI * Radius";
				HintLines[1] = 2;
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

			/** Number of Lessons in the bank */
			private int NUM_OF_LESSONS = 2;
			private int QuestionLines[] = new int[NUM_OF_LESSONS];
			private int HintLines[] = new int[NUM_OF_LESSONS];

			/** Q/A String arrays */
			private String Question[][] = new String[NUM_OF_LESSONS][6]; // 6
																			// lines
			private char Answer[] = new char[NUM_OF_LESSONS];
			private String AnswerList[][] = new String[NUM_OF_LESSONS][2]; // 2
																			// lines
			private String Hint[][] = new String[NUM_OF_LESSONS][10]; // up
																		// to
																		// 10
																		// lines

			/** Lesson assets */
			private Element Objects[][] = new Element[NUM_OF_LESSONS][4]; // 4
																			// ojbects
			private int ObjectCount[] = new int[NUM_OF_LESSONS];

		}

		public static class Unprojector {

			private float depth;

			public Unprojector(float z) {
				depth = z;
			}

			/**
			 * Unproject 2D touch screen coordinates to 3D world
			 * 
			 * @param x
			 * @param y
			 * @return result : type float []
			 */
			private MatrixGrabber mMatrixGrabber = new MatrixGrabber();

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

	}

}
