package com.creatrixelit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.creatrixelit.R;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

/**
 * Template GL Ativity class
 * 
 * @author Creatrix Elit
 *
 */
public class GLRendererActivity extends Activity {
	/** The OpenGL View */
	private GLSurfaceView glSurface;
	private GlRenderer renderer;
	  
	Display display;
	int width, height;
	
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
				
		// here I get the screen info for various calculations
		display = getWindowManager().getDefaultDisplay();
		width = display.getWidth(); // deprecated
		height = display.getHeight(); // deprecated
		
		// Initiate the Open GL view and
        // create an instance with this activity
        glSurface = new GLSurfaceView(this);
        renderer = new GlRenderer();
        glSurface.setRenderer(renderer);
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
	
	@Override
	  public boolean onTouchEvent(MotionEvent event) {
	    switch (event.getAction()) {
	      case MotionEvent.ACTION_MOVE:
	    	  return super.onTouchEvent(event);
	    	  
	      case MotionEvent.ACTION_DOWN:
	    	  return super.onTouchEvent(event);

	      case MotionEvent.ACTION_UP:
	    	  return super.onTouchEvent(event);
	    	  
	      default:
	        return super.onTouchEvent(event);
	    }
	  }
	
	
	/**
	 * Template Renderer class
	 * 
	 * @author Creatrix Elit
	 *
	 */
	public class GlRenderer implements Renderer {
		
		/** Constructor to set the handed over context */
		public GlRenderer() {
			
		}

		@Override
		public void onDrawFrame(GL10 gl) {
			// clear Screen and Depth Buffer
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			// Reset the Modelview Matrix
			gl.glLoadIdentity();
			
			// Draw OpenGL fun

		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			if(height == 0) { 						//Prevent A Divide By Zero By
				height = 1; 						//Making Height Equal One
			}

			gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
			gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
			gl.glLoadIdentity(); 					//Reset The Projection Matrix

			//Calculate The Aspect Ratio Of The Window
			GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);

			gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
			gl.glLoadIdentity(); 					//Reset The Modelview Matrix
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			gl.glEnable(GL10.GL_TEXTURE_2D);		
			gl.glShadeModel(GL10.GL_SMOOTH); 			
			gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 
			gl.glClearDepthf(1.0f); 					
			gl.glEnable(GL10.GL_DEPTH_TEST); 			
			gl.glDepthFunc(GL10.GL_LEQUAL);
			
			//Really Nice Perspective Calculations
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); 

		}

	}
}
