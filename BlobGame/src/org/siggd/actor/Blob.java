package org.siggd.actor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.box2dLight.PointLight;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.siggd.ContactHandler;
import org.siggd.Controllable;
import org.siggd.ControllerFilterAPI;
import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.Level;
import org.siggd.Player;
import org.siggd.Player.ControlType;
import org.siggd.StableContact;
import org.siggd.Timer;
import org.siggd.view.CompositeDrawable;
import org.siggd.view.Drawable;
import org.siggd.view.LevelView;
import org.siggd.view.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.Array;

public class Blob extends Actor implements Controllable {
	private class Spring {
		public int a; // First body
		public int b; // Second body
		public float elasticity; // Hooke's law constant
		public float restLength; // Resting length of string
	}

	private class BlobDrawable implements Drawable {
		SpriteBatch mBatch;
		private Color mSolidColor;
		public Color mSquishColor;
		private Color mCurrentColor;
		private Color mDestColor;
		private float mColorTransSpeed = .15f;
		private float mExtraRadius = 0.012f;
		private Mesh mMesh;
		public Sprite mAccessoryHat;
		public Sprite mAccessoryMouth;
		private Vector2 mBottom;
		private Vector2 mBottomNormal;
		private Vector2 mTop;
		private Vector2 mTopNormal;
		private boolean mDrawTop = false;

		public final String mCrown = "data/gfx/crown.png";
		public final String mCatHat = "data/gfx/cat_hat.png";
		public final String mGloveHat = "data/gfx/rubberglovehat.png";
		public final String mHandlebarMustache = "data/gfx/handlebarmustache.png";

		public ArrayList<Sprite> mHats;

		public BlobDrawable(Color squishColor, Color solidColor) {
			mSquishColor = squishColor;
			mSolidColor = solidColor;
			mCurrentColor = new Color(mSquishColor);
			mDestColor = new Color(mSquishColor);
			float scale = Game.get().getLevelView().getVScale();
			mHats = new ArrayList<Sprite>();
			mHats.add(new Sprite(new Vector2(), new Vector2(16 / scale, 6 / scale), mCrown));
			mHats.add(new Sprite(new Vector2(), new Vector2(16 / scale, 2 / scale), mGloveHat));
			mHats.add(new Sprite(new Vector2(), new Vector2(16 / scale, 6 / scale), mCatHat));
			mAccessoryHat = mHats.get(0);
			mAccessoryMouth = new Sprite(new Vector2(), new Vector2(16 / scale, 16 / scale),
					mHandlebarMustache);
		}

		public void dispose() {
			if (mMesh != null && mMesh.getNumIndices() > 0) {
				mMesh.dispose();
			}
		}

		/**
		 * Calculates smooth curves based on supplied vertices
		 * 
		 * @param vertices
		 *            vertices given vertices used to calculate bezier curves
		 * @param granularity
		 *            number of vertices per original vertex
		 * @return ArrayList of Vector2's to replace previous vertices
		 */
		private ArrayList<Vector2> getBezierVertices(ArrayList<Vector2> vertices, int granularity) {
			ArrayList<Vector2> bezierVertices = new ArrayList<Vector2>();
			int numVertices = vertices.size();
			// bezier: B(t)=(1-t)^2 P0 + 2(1-t)t P1 + t^2 P2
			// http://en.wikipedia.org/wiki/B%C3%A9zier_curve#Quadratic_B.C3.A9zier_curves

			// loop through the vertices and calculate new vertices
			for (int j = 0; j < vertices.size(); j++) {
				Vector2 P1 = vertices.get(j); // current vertex, also the
				// control point
				Vector2 previous = vertices.get((j + numVertices - 1) % numVertices); // previous
																						// vertex,
																						// used
																						// to
																						// calculate
				// preceding midpoint,P0
				Vector2 next = vertices.get((j + numVertices + 1) % numVertices); // next
				// vertex,
				// used to
				// calculate
				// following
				// midpoint,P2
				Vector2 P0 = new Vector2((P1.x - previous.x) / 2 + previous.x, (P1.y - previous.y)
						/ 2 + previous.y);
				Vector2 P2 = new Vector2((next.x - P1.x) / 2 + P1.x, (next.y - P1.y) / 2 + P1.y);
				for (int i = 0; i < granularity; i++) {
					float t = ((float) i) / granularity;
					// bezier: B(t)=(1-t)^2 P0 + 2(1-t)t P1 + t^2 P2
					double x = Math.pow(1 - t, 2) * P0.x + (2.0 * (1 - t) * t) * P1.x
							+ Math.pow(t, 2) * P2.x; // x
					double y = Math.pow(1 - t, 2) * P0.y + (2.0 * (1 - t) * t) * P1.y
							+ Math.pow(t, 2) * P2.y; // y
					bezierVertices.add(new Vector2((float) x, (float) y));
				}
			}
			return bezierVertices;
		}

		@Override
		public void drawSprite(SpriteBatch batch) {
			mBatch = batch;
		}

		/**
		 * Calculates the vertices for mesh
		 * 
		 * @param polygon
		 *            polygon that triangulate() has been called on
		 * @return array of vertices for the mesh
		 */
		public float[] polygonize(Polygon polygon) {
			int vertexIndex = 0;
			int numVertices = polygon.getTriangles().size() * 3; // each
			// particle
			// starts a
			// triangle
			// (1-3)
			// System.out.println("TRIANGULATED: "+numVertices
			// +" | "+numVertices*4);
			float[] verts = new float[numVertices * 3]; // 4 numbers in a row
			// make a vertex, such
			// that:
			// (x,y,color)
			for (int i = 0; i < polygon.getTriangles().size(); i++) {
				// start of the triangle
				verts[vertexIndex++] = polygon.getTriangles().get(i).points[0].getXf();
				verts[vertexIndex++] = polygon.getTriangles().get(i).points[0].getYf();
				verts[vertexIndex++] = mCurrentColor.toFloatBits();

				// next particle, 2nd point in the triangle
				verts[vertexIndex++] = polygon.getTriangles().get(i).points[1].getXf();
				verts[vertexIndex++] = polygon.getTriangles().get(i).points[1].getYf();
				verts[vertexIndex++] = mCurrentColor.toFloatBits();

				// next particle, 3rd point in the triangle
				verts[vertexIndex++] = polygon.getTriangles().get(i).points[2].getXf();
				verts[vertexIndex++] = polygon.getTriangles().get(i).points[2].getYf();
				verts[vertexIndex++] = mCurrentColor.toFloatBits();
			}
			return verts;
		}

		/**
		 * Calculates the vertices for mesh if the polygon method fails
		 * 
		 * @param vertices
		 *            ArrayList of bezier'd points
		 * @param pos
		 *            Center of blob used to create triangles
		 * @return array of vertices for the mesh
		 */
		public float[] polygonize(ArrayList<Vector2> vertices, Vector2 pos) {
			int numVertices = vertices.size() * 3; // each particle starts a
			// triangle (1-3) color at
			// each vertex (4th)
			float[] verts = new float[numVertices * 3]; // 3 numbers in a row
			// make a vertex, such
			// that: (x,y,color)

			// System.out.println("NO triangualte: "+numVertices +
			// " | "+verts.length);
			int vertexIndex = 0;
			for (int i = 0; i < vertices.size(); i++) {
				Vector2 start = vertices.get(i); // current particle, the start
				// of the triangle
				verts[vertexIndex++] = start.x;
				verts[vertexIndex++] = start.y;
				verts[vertexIndex++] = mCurrentColor.toFloatBits();
				// modulo magic to get next vertex even if the next index is out
				// of bounds
				Vector2 end = vertices.get((i + vertices.size() + 1) % vertices.size());

				verts[vertexIndex++] = end.x;
				verts[vertexIndex++] = end.y; // next particle, 2nd point in the
				// triangle
				verts[vertexIndex++] = mCurrentColor.toFloatBits();

				verts[vertexIndex++] = pos.x;
				verts[vertexIndex++] = pos.y; // center of blob, last point in
				// triangle
				verts[vertexIndex++] = mCurrentColor.toFloatBits();
			}
			return verts;
		}

		@Override
		public void drawElse(ShapeRenderer shapeRender) {
			ArrayList<Vector2> vertices = new ArrayList<Vector2>();
			Vector2 pos;
			if (mState == SQUISH_STATE) {
				mDestColor = new Color(mSquishColor);
				calcCenters();
				for (Body b : mParticles) {
					Vector2 tempPos = b.getPosition();
					tempPos.add(new Vector2(0, -mExtraRadius).rotate(b.getAngle() * 180f
							/ (float) Math.PI));
					vertices.add(tempPos);
				}
				pos = mCenterOfMass; // pos is used as the center of the blob
			} else {
				mDestColor = new Color(mSolidColor);
				pos = mBody.getPosition(); // center of the blob
				for (int i = 0; i < mParticles.size(); i++) {
					// get the particle's position
					Vector2 tempPos = mParticles.get(i).getPosition();
					// the relative vector from center to the particle
					tempPos.sub(mCenterOfMass);
					float angle = mBody.getAngle();
					tempPos.add(new Vector2(0, -mExtraRadius).rotate(mParticles.get(i).getAngle()
							* 180f / (float) Math.PI));
					// rotate it based on mBody's rotation
					tempPos.rotate((float) (angle * 180 / Math.PI));
					// add the center of blob to the relative vector
					tempPos.add(pos);
					vertices.add(tempPos);
				}
			}

			// Color Transitioning between states
			float deltaRed = (mDestColor.r - mCurrentColor.r) * mColorTransSpeed;
			float deltaGreen = (mDestColor.g - mCurrentColor.g) * mColorTransSpeed;
			float deltaBlue = (mDestColor.b - mCurrentColor.b) * mColorTransSpeed;
			float deltaAlpha = (mDestColor.a - mCurrentColor.a) * mColorTransSpeed;
			mCurrentColor.add(deltaRed, deltaGreen, deltaBlue, deltaAlpha);

			vertices = getBezierVertices(vertices, 5);

			List<PolygonPoint> points = new ArrayList<PolygonPoint>();
			int bezierSize = vertices.size(); // size of the bezier point
			for (int i = 0; i < vertices.size(); i++) {
				PolygonPoint p = new PolygonPoint(vertices.get(i).x, vertices.get(i).y);
				points.add(p);
			}

			for (int i = 0; i < points.size(); i++) {
				PolygonPoint p = points.get(i);
				p.setPrevious(points.get((i + bezierSize - 1) % bezierSize));
				p.setNext(points.get((i + bezierSize + 1) % bezierSize));
			}
			Polygon polygon = new Polygon(points);
			float[] verts;
			int numVertices;
			try {
				Poly2Tri.triangulate(polygon);
				verts = polygonize(polygon);
			} catch (RuntimeException e) {
				verts = polygonize(vertices, pos);
			}

			// verts has an x,y,color
			numVertices = verts.length / 3;

			// Make sure we have a mesh that can fit the vertices
			if (mMesh == null) {
				mMesh = new Mesh(false, numVertices, 0, new VertexAttribute(Usage.Position, 2,
						"a_position"), new VertexAttribute(Usage.ColorPacked, 4, "a_color"));
			} else if (numVertices > mMesh.getMaxVertices()) {
				int newNumVertices = mMesh.getMaxVertices();

				mMesh.dispose();

				// Dynamic array style resizing
				while (newNumVertices < numVertices)
					newNumVertices *= 2;

				mMesh = new Mesh(false, newNumVertices, 0, new VertexAttribute(Usage.Position, 2,
						"a_position"), new VertexAttribute(Usage.ColorPacked, 4, "a_color"));
			}

			// openGL settings
			mMesh.setVertices(verts);// set the mesh vertices to the vertices
			if (mSolidColor.a < 1) {
				Gdx.gl.glEnable(GL10.GL_BLEND);
			}
			if (Gdx.graphics.isGL20Available()) {
				ShaderProgram shader = Game.get().getLevelView().getDefaultShaderProgram();
				shader.begin();
				mMesh.bind(shader);
				shader.setUniformMatrix("u_worldView",
						Game.get().getLevelView().getCamera().combined);
				mMesh.render(shader, GL20.GL_TRIANGLES);
				shader.end();
				mMesh.unbind(shader);
			} else {
				GLCommon gl10 = Gdx.graphics.getGLCommon();
				gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
				gl10.glEnable(GL10.GL_BLEND);
				mMesh.render(GL10.GL_TRIANGLES, 0, numVertices); // render
			}
			if (mSolidColor.a < 1) {
				Gdx.gl.glDisable(GL10.GL_BLEND);
			}

			// set outline for blobs
			shapeRender.begin(ShapeType.Line);
			Color c = mCurrentColor.cpy().mul(.6f, .6f, .6f, 1);
			c.a = 1;
			shapeRender.setColor(c);

			float lineWidth = 3 / (120 * Game.get().getLevelView().getScale());

			lineWidth *= Math.min(Gdx.graphics.getWidth() / LevelView.vWIDTH,
					Gdx.graphics.getHeight() / LevelView.vHEIGHT);

			for (int i = 0; i < vertices.size(); i++) {
				Vector2 v1;
				Vector2 v2;
				v1 = vertices.get(i).cpy();
				v2 = vertices.get((i + 1) % vertices.size()).cpy();
				Vector2 offset = v1.cpy().sub(v2).scl(.03f);
				v1.add(offset);
				v2.sub(offset);
				shapeRender.line(v1.x, v1.y, v2.x, v2.y);
			}

			GLCommon gl10 = Gdx.graphics.getGLCommon();
			gl10.glEnable(GL10.GL_BLEND);
			if (!Gdx.graphics.isGL20Available()) {
				gl10.glLineWidth(lineWidth);
				shapeRender.end();
				gl10.glLineWidth(1);
			} else {
				Gdx.gl20.glLineWidth(lineWidth);
				shapeRender.end();
				Gdx.gl20.glLineWidth(1);
			}

			gl10.glDisable(GL10.GL_BLEND);
			if (mBatch == null)
				return;
			mBatch.begin();
			drawAccessories(vertices);
			drawEyes();
			mBatch.end();
		}

		private void drawAccessories(ArrayList<Vector2> vertices) {
			Vector2 eyeGap = mLeftEye.getPosition().cpy().add(mRightEye.getPosition()).div(2f);
			float rotation = mRightEye.getPosition().cpy().sub(mLeftEye.getPosition()).angle();
			Vector2 perpendicular = new Vector2(0, 3).rotate(rotation);
			Vector2 parallel = new Vector2(1, 0).rotate(rotation);
			Vector2 projection = eyeGap.cpy().add(perpendicular);
			Vector2 projection2 = eyeGap.cpy().sub(perpendicular);
			mTop = null;
			mTopNormal = null;
			Vector3 biggestintersect = new Vector3(0, 0, -Float.MAX_VALUE);
			Vector3 smallestintersect = new Vector3(0, 0, Float.MAX_VALUE);
			float smallz = Float.MAX_VALUE;
			for (int i = 0; i < vertices.size(); i++) {
				Vector2 v1;
				Vector2 v2;
				v1 = vertices.get(i);
				v2 = vertices.get((i + 1) % vertices.size());
				Vector3 intersect = segmentIntersection(v1.cpy(), v2.cpy(), projection2.cpy(),
						projection.cpy());
				if (intersect != null) {
					if (biggestintersect.z < intersect.z) {
						biggestintersect = intersect;
						mTop = new Vector2(intersect.x, intersect.y);
						mTopNormal = v1.cpy().sub(v2);
					}
					if (smallestintersect.z > intersect.z) {
						smallestintersect = intersect;
						mBottom = new Vector2(intersect.x, intersect.y);
						mBottomNormal = v1.cpy().sub(v2);
						smallz = intersect.z;
					}
				}
			}
			if (mTop != null && mDrawTop && mAccessoryHat != null) {
				mAccessoryHat.mPosition = mTop;
				mAccessoryHat.mAngle = mTopNormal.angle();
				mAccessoryHat.drawSprite(mBatch);
			}
			if (mBottom != null) {
				mAccessoryMouth.mPosition = eyeGap.cpy().sub(perpendicular.cpy().div(26))
						.add(parallel.scl(mLastKnownDir ? .02f : -.02f));
				if (mState == SQUISH_STATE) {
					mBottomNormal.y /= 5;
					mAccessoryMouth.mAngle = mBottomNormal.angle() + 180f;
				} else {
					mAccessoryMouth.mAngle = rotation;
				}
				Player p = Game.get().getPlayer(getmPlayerID());
				if (p != null && p.mustache)
					mAccessoryMouth.drawSprite(mBatch);
			}
		}

		public Vector3 segmentIntersection(Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4) {
			Vector2 p = p1;
			Vector2 r = p2.sub(p1);
			Vector2 q = p3;
			Vector2 s = p4.sub(p3);
			Vector2 qSubP = q.cpy().sub(p);
			float rCrossS = cross(r, s);
			if (rCrossS < 0.0000001 && rCrossS > -0.000001)
				return null;
			// t = (q - p) � s / (r � s)
			float t = cross(qSubP, s) / rCrossS;
			// u = (q - p) � r / (r � s)
			float u = cross(qSubP, r) / rCrossS;
			if (u >= 0 && t >= 0 && u <= 1 && t <= 1) {
				Vector2 solution = q.add(s.scl(u));
				return new Vector3(solution.x, solution.y, u);
			}
			return null;
		}

		public float cross(Vector2 v1, Vector2 v2) {
			return v1.x * v2.y - v1.y * v2.x;
		}

		/**
		 * Draws the eyes!!
		 */
		public void drawEyes() {

			AssetManager man = Game.get().getAssetManager();

			// Get Scale
			float scale = Game.get().getLevelView().getVScale();

			if (man.isLoaded(mSquishEye) && man.isLoaded(mSolidEye)) {
				Texture tex;
				Vector2 center;
				float rotation;
				if (mState == SQUISH_STATE) {
					tex = man.get(mSquishEye, Texture.class);
					center = new Vector2(mCenterOfMass);
					center.add(mVCenter.scl(.055f));
					rotation = mRightEye.getPosition().cpy().sub(mLeftEye.getPosition()).angle();
				} else {
					tex = man.get(mSolidEye, Texture.class);
					center = new Vector2(mBody.getPosition());
					center.add(mBody.getLinearVelocity().scl(.055f));
					rotation = (float) (mBody.getAngle() * 180.0f / Math.PI);
				}
				Vector2 trueCenter = new Vector2(center);
				Vector2 texSize = new Vector2(tex.getWidth(), tex.getHeight());
				Vector2 texOffset = new Vector2(texSize.x / 2 / scale, texSize.y / 2 / scale);
				Vector2 origin = new Vector2(DEFAULT_RADIUS / 2f, DEFAULT_RADIUS / 2f);

				center = mRightEye.getPosition();
				Vector2 deltaPosition = new Vector2(center);
				deltaPosition.sub(trueCenter);
				float eyescale = ((float) Math.max(1.2 - deltaPosition.len() * .25, 1f));
				eyescale *= .45;
				if (mState == SQUISH_STATE)
					eyescale += mAccAprox * .12;
				mBatch.draw(tex, center.x - texOffset.x, center.y - texOffset.y, origin.x,
						origin.y, texSize.x / scale, texSize.y / scale, eyescale, eyescale,
						rotation, 0, 0, (int) texSize.x, (int) texSize.y, mLastKnownDir, false);

				center = mLeftEye.getPosition();
				deltaPosition = new Vector2(center);
				deltaPosition.sub(trueCenter);
				eyescale = ((float) Math.max(1.2 - deltaPosition.len() * .25, 1f));
				eyescale *= .45;
				if (mState == SQUISH_STATE)
					eyescale += mAccAprox * .12;
				mBatch.draw(tex, center.x - texOffset.x, center.y - texOffset.y, origin.x,
						origin.y, texSize.x / scale, texSize.y / scale, eyescale, eyescale,
						rotation, 0, 0, (int) texSize.x, (int) texSize.y, mLastKnownDir, false);

			}
		}

		@Override
		public void drawDebug(Camera camera) {
			// TODO Auto-generated method stub
		}
	}

	public static Color COLORS[] = { new Color(0f, .8f, 0f, 1f), // Green
			new Color(.2f, .25f, .95f, 1f), // Blue
			new Color(.9f, 0f, 0f, 1f), // Red
			new Color(1f, .5f, 0f, 1f), // Orange
			new Color(1f, 1f, .0f, 1f), // Yellow
			new Color(.5f, .12f, 1f, 1f), // Purple
			new Color(.21f, .71f, .9f, 1f), // Cyan
			// new Color(.3f, .1f, 0f, 1f), // Dark Red
			new Color(0f, .3f, 0f, 1f), // Dark Green
			new Color(1f, .5f, .9f, 1f), // Pink
			// new Color(0f, 0f, 0f, 1f), // Black
			// new Color(1f, 1f, 1f, 1f), // White
			// new Color(1f, 1f, 1f, 1f), // White
			new Color(.8f, .8f, .8f, .5f) // Gray

	};
	
	public static Color colors(int i)
	{
		if(Game.CALM) return CALM_COLORS[i];
		return COLORS[i];
	}
	
	public static void setColor(int i, Color x)
	{
		if(!Game.CALM)Blob.COLORS[i] = x;
		else Blob.CALM_COLORS[i] = x;
	}
	
	public static Color CALM_COLORS[] = { 
		new Color(0.3f, .7f, 0.3f, 1f), // Green
		new Color(.2f, .25f, .8f, 1f), // Blue
		new Color(.7f, .1f, 0.1f, 1f), // Red
		new Color(.8f, .4f, .1f, 1f), // Orange
		new Color(.8f, .8f, .2f, 1f), // Yellow
		new Color(.6f, .36f, .9f, 1f), // Purple
		new Color(.21f, .5f, .7f, 1f), // Cyan
		new Color(0.05f, .3f, 0.05f, 1f), // Dark Green
		new Color(.8f, .5f, .6f, 1f), // Pink
		new Color(.8f, .8f, .8f, .5f) // Gray

		};

	private static final int SQUISH_STATE = 0; // /< The number of particles to
	// use
	private static final int SOLID_STATE = 1; // /< The number of particles to
	// use

	private static final int NUM_PARTICLES = 20; // /< The number of particles
	// to use
	private static final float DEFAULT_RADIUS = .5f; // /< The default radius of
	// blob (on creation)

	// Force Constants
	private static float LATERAL_FORCE = .15f;
	private static float ROTATION_FORCE = .18f;
	private static float ROTATION_MULT_IF_GRABBING = 1f;
	private static float DAMPENING_COEFF = 0.1f;
	private static float POOF_COEFF = 8f;
	private static float SOLID_MASS_MULT = 2f;
	private static float GRAB_BREAK_FORCE = 300;

	private ArrayList<Body> mParticles; // /< The particles that compose the
	// blob
	private ArrayList<Vector2> mCachedEdge; // /< exact edge formed by blob's
	// particles
	private ArrayList<Vector2> mCachedInnerEdge; // /< slightly smaller edge
	// formed by blob's
	// particles
	private ArrayList<Vector2> mCachedOuterEdge; // /< slightly larger edge
	// formed by blob's
	// particles
	private ArrayList<Spring> mSprings; // /< The spring constraints
	private ArrayList<Joint> mJoints; // /< joints from grabbing
	private Vector2 mLeftEyeDest;
	private Vector2 mRightEyeDest;
	private Vector2 mCenterOfMass;
	private Vector2 mOldVCenter;
	private Vector2 mVCenter;
	private float mPoof;
	private boolean mGrabbing;
	private String mMow = "data/sfx/mow.wav";
	private String mNom = "data/sfx/nom.wav";
	private String mSquishEye = "data/gfx/EYE.png";
	private String mSolidEye = "data/gfx/EYE2.png";
	private String mBlobGradient = "data/gfx/blobgradient.png";
	public boolean mSpawning = false;
	private Body mLeftEye;
	private Body mRightEye;
	private Filter mEyeFilter;
	private Vector2 mEyeOffset = new Vector2(.15f, .05f);
	private boolean mLastKnownDir = true;
	private int mDirection = 0;
	Timer mSoundTimer;
	private final float mDensityDivisor = 5f; // /< Since blob is pretending
	// to have a larger area than
	// its bodies
	// actually cover, we need
	// to scale our displayed
	// density
	private float mAccAprox = 0; // < approximates acceleration of squishy blob
	private PointLight mLight;
	float mExtraGlow = -100;
	private Color mLightColor;
	private BlobDrawable mBlobDrawable;
	private int mPointCombo = 0;
	private int mPoints = 0;
	private boolean mWasDown;
	private boolean mWasUp = true;
	public boolean mFinishedLevel = false;
	private int mPoofTimer = 0;

	private boolean mGettingPulled = false;

	private int mState = SQUISH_STATE;

	/**
	 * Constructor. No non-optional parameters may be added to this constructor.
	 * This should contain only properties, and code that MUST run before later
	 * init,
	 * 
	 * @param level
	 *            The level that contains this actor
	 * @param id
	 *            The id
	 */
	public Blob(Level level, long id) {
		super(level, id);
		mName = "Blob";
		mParticles = new ArrayList<Body>();
		mSprings = new ArrayList<Spring>();
		mJoints = new ArrayList<Joint>();
		mVCenter = new Vector2(0, 0);
		mOldVCenter = new Vector2();
		mLeftEyeDest = new Vector2(0, 0);
		mRightEyeDest = new Vector2(0, 0);

		mPoof = 1;

		if (getLevel().getAssetKey() != null) {
			Color squishColor = new Color(0f, .9f, 0f, 1f);
			Color solidColor = new Color(squishColor);
			solidColor.mul(.7f, .7f, .7f, 1f);
			mBlobDrawable = new BlobDrawable(squishColor, solidColor);
			((CompositeDrawable) mDrawable).mDrawables.add(mBlobDrawable);
		}
		makeBlobBody();
		if (LevelView.mUseLights) {
			mLight = new PointLight(Game.get().getLevelView().getRayHandler(), 256, new Color(0f,
					0f, 0f, 1f), 7, -10000, -10000);
			mLight.setSoft(true);
			mLight.setSoftnessLenght(.5f);
			mLight.setActive(false);
			mLight.attachToBody(mLeftEye, 0f, 0f);
		}
		mSoundTimer = new Timer();
		mSoundTimer.setTimer(12);
		mSoundTimer.unpause();
		setProp("Player ID", (Integer) 0);
	}

	/**
	 * Draw the blob
	 */
	@Override
	public void draw() {
		// TODO: Draw something here
	}

	/**
	 * Load resources needed by the actor
	 */
	@Override
	public void loadResources() {
		AssetManager man = Game.get().getAssetManager();
		man.load(mBlobGradient, Texture.class);
		man.load(mSquishEye, Texture.class);
		man.load(mSolidEye, Texture.class);

		if (getLevel().getAssetKey() != null) {
			man.load(mBlobDrawable.mCrown, Texture.class);
			man.load(mBlobDrawable.mCatHat, Texture.class);
			man.load(mBlobDrawable.mGloveHat, Texture.class);
			man.load(mBlobDrawable.mHandlebarMustache, Texture.class);
			// man.load(mBlobDrawable.mMouth, Texture.class);
		}

		man.load(mMow, Sound.class);
		man.load(mNom, Sound.class);
	}

	/**
	 * Load bodies needed by the actor
	 */
	@Override
	public void loadBodies() {
	}

	/**
	 * Dispose of the actor's resources
	 */
	@Override
	public void dispose() {
		AssetManager man = Game.get().getAssetManager();
		// man.unload(mBlobGradient);
		// man.unload(mSquishEye);
		// man.unload(mSolidEye);
		/*
		 * if (man.isLoaded(mMow)) { man.unload(mMow); } if (man.isLoaded(mNom))
		 * { man.unload(mNom); }
		 */
		mBlobDrawable.dispose();
	}

	@Override
	public void destroy() {
		Game.get().getLevel().getWorld().destroyBody(mBody);
		for (Body b : mParticles)
			// Destroy All Blob Particles
			Game.get().getLevel().getWorld().destroyBody(b);
		mParticles = new ArrayList<Body>();
	}

	/**
	 * radius < 0 means use cached inner edge radius = 0 means use cached edge
	 * radius > 0 means use cached outer edge
	 */
	public boolean isPointInBlob(Vector2 point, float radius) {
		int i;
		int j;
		boolean result = false;
		Vector2 vi;
		Vector2 vj;
		if (mCachedEdge == null) {
			mCachedEdge = new ArrayList<Vector2>();
			mCachedInnerEdge = new ArrayList<Vector2>();
			mCachedOuterEdge = new ArrayList<Vector2>();
			if (mState == SOLID_STATE) {
				Vector2 pos = mBody.getPosition(); // center of the blob
				for (i = 0; i < mParticles.size(); i++) {
					Vector2 tempPos = mParticles.get(i).getPosition(); // get
					// the
					// particle's
					// position
					tempPos.sub(mCenterOfMass); // the relative vector from
					// center to the particle
					float angle = mBody.getAngle();
					Vector2 tempPos2 = tempPos.cpy();
					Vector2 tempPos3 = tempPos.cpy();
					tempPos.rotate((float) (angle * 180 / Math.PI)); // rotate
					// it
					// based
					// on
					// mBody's
					// rotation
					tempPos.add(pos);
					Vector2 buffer = new Vector2(0, -.05f).rotate(mParticles.get(i).getAngle()
							* 180f / (float) Math.PI);
					tempPos2.add(buffer);
					tempPos2.rotate((float) (angle * 180 / Math.PI));
					tempPos2.add(pos);
					tempPos3.sub(buffer);
					tempPos3.rotate((float) (angle * 180 / Math.PI));
					tempPos3.add(pos);
					mCachedEdge.add(tempPos);
					mCachedOuterEdge.add(tempPos2);
					mCachedInnerEdge.add(tempPos3);

				}
			} else {
				for (i = 0; i < mParticles.size(); i++) {
					Vector2 tempPos = mParticles.get(i).getPosition();
					Vector2 tempPos2 = tempPos.cpy();
					Vector2 tempPos3 = tempPos.cpy();
					Vector2 buffer = new Vector2(0, -.05f).rotate(mParticles.get(i).getAngle()
							* 180f / (float) Math.PI);
					tempPos2.add(buffer);
					tempPos3.sub(buffer);
					mCachedEdge.add(tempPos);
					mCachedOuterEdge.add(tempPos2);
					mCachedInnerEdge.add(tempPos3);
				}
			}
		}

		ArrayList<Vector2> edges;
		if (radius == 0) {
			edges = mCachedEdge;
		} else if (radius < 0) {
			edges = mCachedInnerEdge;
		} else {
			edges = mCachedOuterEdge;
		}

		for (i = 0, j = edges.size() - 1; i < edges.size(); j = i++) {
			vi = edges.get(i);
			vj = edges.get(j);
			if ((vi.y > point.y) != (vj.y > point.y)
					&& (point.x < (vj.x - vi.x) * (point.y - vi.y) / (vj.y - vi.y) + vi.x)) {
				result = !result;
			}
		}
		return result;
	}

	public boolean isSolid() {
		return mState == SOLID_STATE;
	}

	/**
	 * Set a category group to all fixtures of blob. Overridden in order to
	 * handle the numerous different features of blob.
	 */
	@Override
	public void setCollisionGroup(int group) {
		// Create a filter
		Filter filter;
		Array<Fixture> fixtures;

		// Set all of the fixtures in the bodies of mParticles to the same same
		// Collision Group.
		if (mState == SQUISH_STATE) {
			for (Body b : mSubBodies) {
				fixtures = b.getFixtureList();
				for (Fixture f : fixtures) {
					filter = f.getFilterData(); // Get the Current Filter data
					filter.groupIndex = (short) group; // Change the group index
					f.setFilterData(filter); // Apply the new filter to the
					// fixture.
				}
			}
		}

		// Set all fixtures of the mBody to the same Collision Group.
		fixtures = mBody.getFixtureList();
		for (Fixture f : fixtures) {
			filter = f.getFilterData();
			filter.groupIndex = (short) group;
			f.setFilterData(filter);
		}
	}

	private void calcCenters() {
		// Determine velocity of center of mass (this is the velocity of the
		// blob, as a whole system)
		mVCenter = new Vector2();
		// Find average position of all particles,
		mCenterOfMass = new Vector2();

		for (Body b : mParticles) {
			mVCenter.add(b.getLinearVelocity());
			mCenterOfMass.add(b.getPosition());
		}
		mVCenter.scl(1f / mParticles.size());
		mCenterOfMass.scl(1.0f / NUM_PARTICLES);
	}

	/**
	 * Called to perform actor logic
	 */
	@Override
	public void update() {
		mCachedEdge = null;
		mCachedInnerEdge = null;
		mCachedOuterEdge = null;
		calcCenters();

		Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler()
				.getContacts(this);
		Iterable<Actor> actors = ContactHandler.getActors(contacts);
		// Do all string constraints
		for (Spring s : mSprings) {
			Body a = mParticles.get(s.a);
			Body b = mParticles.get(s.b);

			Vector2 forceDir = b.getPosition().cpy();
			forceDir.sub(a.getPosition());

			// Normalize force
			float len = forceDir.len();
			forceDir.nor();

			// Determine the effective spring constant
			float k = Math.min(4, s.elasticity * (mVCenter.len() * .5f + 1) * mPoof);

			// Apply Hooke's law
			float diff = (len - s.restLength) * k;

			forceDir.scl(diff);

			// Apply spring forces
			a.applyForceToCenter(forceDir.x, forceDir.y, true);
			b.applyForceToCenter(-forceDir.x, -forceDir.y, true);

			// Determine coefficient of critical damping (from Wikipedia)
			float damp = 2 * DAMPENING_COEFF * (float) Math.sqrt(a.getMass() * k);

			// Project velocity vectors onto force dir (formula from Wikipedia:
			// Vector Projection)
			Vector2 dampForce = b.getPosition().cpy();
			dampForce.sub(a.getPosition());
			dampForce.nor();

			// These are the velocities of the particles
			Vector2 vel = b.getLinearVelocity().cpy();
			vel.sub(a.getLinearVelocity());

			dampForce.scl(damp * dampForce.dot(vel));

			a.applyForceToCenter(dampForce, true);
			b.applyForceToCenter(new Vector2(-dampForce.x, -dampForce.y), true);
		}

		Vector2 eyeDelta = new Vector2(mLeftEyeDest);
		eyeDelta.sub(mLeftEye.getPosition());
		mLeftEye.applyForceToCenter(eyeDelta.scl(20f), true);

		eyeDelta = new Vector2(mRightEyeDest);
		eyeDelta.sub(mRightEye.getPosition());
		mRightEye.applyForceToCenter(eyeDelta.scl(20f), true);
		if (mExtraGlow > 0) {
			mExtraGlow -= .5f;
		} else {
			if (mExtraGlow < -.5f)
				mExtraGlow += .5f;
			mPointCombo = 0;
		}
		if (mLight != null) {
			//Gray is the only color with alpha=0.5
			//So light alpha is 1.0 To be brighter
			mLightColor.set(mBlobDrawable.mCurrentColor);
			if(mLightColor.a != 1.0f){
				mLightColor.a = 1.0f;
			}
			float brightness = .12f + (mExtraGlow / (2 * (400 + mExtraGlow)));
			float diff = Convert.getFloat(Game.get().getLevel().getProp("Difficulty"));
			brightness *= 1 + (diff * .25f);
			if (brightness < 0) {
				brightness = 0;
			}
			if (brightness > 1) {
				brightness = 1;
			}
			mLightColor.mul(brightness, brightness, brightness, 1f);
			mLight.setColor(mLightColor);
			mLight.setDistance(3f + 10f * brightness);
		}
		Vector2 center;
		float rotation;
		if (mState == SQUISH_STATE) {
			center = new Vector2(mCenterOfMass);
			center.add(mVCenter.scl(.04f));
			rotation = 0;
		} else {
			center = new Vector2(mBody.getPosition());
			center.add(mBody.getLinearVelocity().scl(.04f));
			rotation = (float) (mBody.getAngle() * 180.0f / Math.PI);
		}

		Vector2 eyeOffset = mEyeOffset.cpy();
		float secondEyeRot = 180.0f - 2 * eyeOffset.angle();
		eyeOffset.rotate(rotation);

		mRightEyeDest.x = center.x + eyeOffset.x;
		mRightEyeDest.y = center.y + eyeOffset.y;
		eyeOffset.rotate(secondEyeRot);
		mLeftEyeDest.x = center.x + eyeOffset.x;
		mLeftEyeDest.y = center.y + eyeOffset.y;

		if (Game.get().getState() == Game.PLAY || Game.get().getState() == Game.MENU) {
			// Key Bindings
			boolean right = false;
			boolean left = false;
			boolean up = false;
			boolean down = false;
			int playerID = Convert.getInt(getProp("Player ID"));
			Player p = Game.get().getPlayer(playerID);
			if (p != null && p.active && p.controltype == ControlType.Controller) {
				// FIX THE AXES LATER
				Controller c = p.controller;
				right = c.getAxis(ControllerFilterAPI.getAxisFromFilteredAxis(c, 1)) > 0.5;
				right |= c.getPov(0) == PovDirection.east;
				right |= c.getPov(0) == PovDirection.northEast;
				right |= c.getPov(0) == PovDirection.southEast;
				left = c.getAxis(ControllerFilterAPI.getAxisFromFilteredAxis(c, 1)) < -0.5;
				left |= c.getPov(0) == PovDirection.west;
				left |= c.getPov(0) == PovDirection.northWest;
				left |= c.getPov(0) == PovDirection.southWest;
				up = c.getButton(ControllerFilterAPI.getButtonFromFilteredId(c,
						ControllerFilterAPI.BUTTON_A));
				up |= c.getPov(0) == PovDirection.north;
				up |= c.getPov(0) == PovDirection.northEast;
				up |= c.getPov(0) == PovDirection.northWest;
				down = c.getButton(ControllerFilterAPI.getButtonFromFilteredId(c,
						ControllerFilterAPI.BUTTON_B));
				down |= c.getPov(0) == PovDirection.south;
				down |= c.getPov(0) == PovDirection.southEast;
				down |= c.getPov(0) == PovDirection.southWest;
			} else if (p != null && p.active) {
				if (p.controltype == ControlType.Arrows) {
					right = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
					left = Gdx.input.isKeyPressed(Input.Keys.LEFT);
					up = Gdx.input.isKeyPressed(Input.Keys.UP);
					down = Gdx.input.isKeyPressed(Input.Keys.DOWN);
				} else if (p.controltype == ControlType.WASD) {
					right = Gdx.input.isKeyPressed(Input.Keys.D);
					left = Gdx.input.isKeyPressed(Input.Keys.A);
					up = Gdx.input.isKeyPressed(Input.Keys.W);
					down = Gdx.input.isKeyPressed(Input.Keys.S);
				}
			}
			if ("earth".equals(Game.get().getLevel().getAssetKey())) {
				right = true;
				left = false;
				up = false;
				down = false;
			}
			if (right && !left) {
				mDirection = 1;
			} else if (left && !right) {
				mDirection = -1;
			} else {
				mDirection = 0;
			}

			if (up) {
				mPoofTimer++;
			} else {
				mPoofTimer = 0;
			}
			if (mPoofTimer > 15) {
				up = false;
			}

			if (!mWasDown && down) {
				transform();
			}
			if (!mWasUp && up && mState == SOLID_STATE) {
				transform();
			}
			if (!mWasUp && up && "charselect".equals(Game.get().getLevel().getAssetKey())) {
				Game.get().getPlayer(getmPlayerID()).swapColor();
				resetColor(getmPlayerID(), false);
			}

			mWasDown = down;
			mWasUp = up;

			// Check if keys are enabled in the level
			up &= Convert.getInt(getLevel().getProp("Puff Enabled")) == 1;

			if (up) {
				mAccAprox += .1f;
			}

			if (mState == SQUISH_STATE) {
				if (right) {
					for (int i = 0; i < NUM_PARTICLES; i++) {
						Body curB = mParticles.get(i); // Current Body Position
						// vector
						Body nextB = mParticles.get((i + NUM_PARTICLES - 1) % NUM_PARTICLES); // Next
																								// Body
																								// Position
																								// vector
						Vector2 v = new Vector2(nextB.getPosition());
						v.sub(curB.getPosition());
						v.nor();
						// Directional Vector * Rotate Force constant + Right
						// Directional Force * Magical Force constant
						v = v.scl(ROTATION_FORCE);
						if (curB.getLinearVelocity().len() > 3) {
							v = new Vector2(0, 0);
						}
						v.add(new Vector2(1, 0).scl(LATERAL_FORCE));
						curB.applyForceToCenter(v, true);
						mLastKnownDir = true;
					}
				}

				if (left) {
					for (int i = 0; i < NUM_PARTICLES; i++) {
						Body curB = mParticles.get(i); // Current Body
						Body nextB = mParticles.get((i + 1) % NUM_PARTICLES); // Body
						// to
						// Vector
						// to;
						Vector2 v = new Vector2(nextB.getPosition());
						v.sub(curB.getPosition());
						v.nor();
						// Directional Vector * Rotate Force constant + Right
						// Directional Force * Magical Force constant
						v = v.scl(ROTATION_FORCE);
						if (curB.getLinearVelocity().len() > 3) {
							v = new Vector2(0, 0);
						}
						v.add(new Vector2(-1, 0).scl(LATERAL_FORCE));
						mParticles.get(i).applyForceToCenter(v, true);
						mLastKnownDir = false;
					}
				}
			} else {
				float mult = mGrabbing ? ROTATION_MULT_IF_GRABBING : 1;
				if (right) {
					mBody.applyAngularImpulse(-.2f * SOLID_MASS_MULT * mult * ROTATION_FORCE
							/ (1 + Math.abs(mBody.getAngularVelocity())), true);
					mBody.applyForceToCenter(
							new Vector2(10 * SOLID_MASS_MULT, 0).scl(LATERAL_FORCE), true);
					mLastKnownDir = true;
				}
				if (left) {
					mBody.applyAngularImpulse(.2f * SOLID_MASS_MULT * mult * ROTATION_FORCE
							/ (1 + Math.abs(mBody.getAngularVelocity())), true);
					mBody.applyForceToCenter(
							new Vector2(-10 * SOLID_MASS_MULT, 0).scl(LATERAL_FORCE), true);
					mLastKnownDir = false;
				}
			}

			if (up && !mGettingPulled) {

				mPoof = POOF_COEFF;
			} else {
				mPoof = 1;
			}

			setPulling(false);
		}

		if (Game.get().getState() == Game.PLAY || Game.get().getState() == Game.MENU) {
			Vector2 vel;
			float threshold = 3f;
			if (mState == SQUISH_STATE) {
				calcCenters();
				vel = new Vector2(mVCenter);
			} else {
				threshold = 6f;
				vel = new Vector2(mBody.getLinearVelocity());
			}
			vel.sub(mOldVCenter);
			float velLength = vel.len();
			mAccAprox += velLength / 10f;
			mAccAprox *= .9f;
			if (velLength > threshold && mSoundTimer.isTriggered()
					&& !"earth".equals(Game.get().getLevel().getAssetKey())

			) {
				AssetManager man = Game.get().getAssetManager();
				Sound sound;
				long soundID;
				if (man.isLoaded(mMow)) {
					sound = man.get(mMow, Sound.class);
					if (mState == SOLID_STATE) {
						soundID = sound.play();
						sound.setPitch(soundID, .5f + velLength * .0001f);
						sound.setVolume(soundID, Math.min(1f, .6f + velLength * .002f));
					} else {
						if (!mSpawning) {
							soundID = sound.play();
							sound.setPitch(soundID, 1f + velLength * .005f);
							sound.setVolume(soundID, Math.min(1f, .6f + velLength * .005f));
						}
					}
				}
				mSoundTimer.reset();
			}
			mSpawning = false;
		}
		if (mState == SQUISH_STATE) {
			mOldVCenter = new Vector2(mVCenter);
		} else {
			mOldVCenter = new Vector2(mBody.getLinearVelocity());
		}
		inOtherBlobs();
		mSoundTimer.update();

		// Break any strained joints
		breakStrainedJoints();
	}

	public void inOtherBlobs() {
		ArrayList<Blob> blobs = mLevel.getBlobs(true);
		blobs.remove(this);
		for (Body body : mSubBodies) {
			Fixture f = body.getFixtureList().get(0);
			ArrayList<Blob> violees = (ArrayList<Blob>) f.getUserData();
			for (Blob blob : blobs) {
				if (violees.contains(blob)) {
					if (!blob.isPointInBlob(body.getPosition(), 0)) {
						violees.remove(blob);
					}
				} else {
					if (blob.isPointInBlob(body.getPosition(), -1)) {
						violees.add(blob);
					}
				}
			}
		}
	}

	public int getmPlayerID() {
		return Convert.getInt(getProp("Player ID"));
	}

	public void eatDot() {
		if ("base".equals(Game.get().getLevel().getAssetKey())) {
			mExtraGlow += 15f;
		} else {
			mExtraGlow += 50f;
		}
		mPoints++;

		AssetManager man = Game.get().getAssetManager();
		if (man.isLoaded(mNom)) {
			Sound sound = man.get(mNom, Sound.class);
			long soundID = sound.play();
			sound.setVolume(soundID, .6f);

			float pitch = 1.0f;
			for (int i = 0; i < mPointCombo; i++) {
				pitch *= 1.05946 * 1.05946;
				if (i == 3 || i == 6)
					pitch /= 1.05946; // This makes it increase along a Major
										// scale, the happiest scale in the
										// universe
			}
			if (pitch > 2)
				pitch = 2;
			sound.setPitch(soundID, pitch);
			mSoundTimer.reset();
		}
		mPointCombo++;

		if (Game.get().activePlayersNum() > 1) {
			ArrayList<Blob> blobs = Game.get().getLevel().getBlobs(false);
			for (Blob blob : blobs) {
				if (!blob.equals(this)) {
					if (mPoints < blob.mPoints) {
						return;
					}
					blob.mBlobDrawable.mDrawTop = false;
				}
			}
			mBlobDrawable.mDrawTop = true;
		}
	}

	@Override
	public void setActive(boolean active) {
		if (!active) {
			mBody.setActive(false);
			for (Body b : mParticles) {
				b.setActive(false);
			}
			if (mLight != null)
				mLight.setActive(false);
			mLeftEye.setActive(false);
			mRightEye.setActive(false);
		} else {
			if (mState == SOLID_STATE) {
				mBody.setActive(true);
			} else {
				for (Body b : mParticles) {
					b.setActive(true);
				}
			}
			if (mLight != null)
				mLight.setActive(true);
			mLeftEye.setActive(true);
			mRightEye.setActive(true);
		}
		mActive = active;
	}

	public void applyForce(Vector2 force) {
		if (mState == SOLID_STATE) {
			mBody.applyForceToCenter(force, true);
		} else {
			force.scl(1f / mSubBodies.size());
			for (Body b : mSubBodies) {
				b.applyForceToCenter(force, true);
			}
		}
	}

	@Override
	public <T> T getProp(String name) {
		if (name.equals("Density")) {
			if (mState == SOLID_STATE) {
				return (T) (Float) (mBody.getFixtureList().get(0).getDensity() / mDensityDivisor);
			} else {
				return (T) (Float) (mParticles.get(0).getFixtureList().get(0).getDensity() / mDensityDivisor);
			}
		}
		if (name.equals("Velocity X")) {
			if (mState == SQUISH_STATE) {
				calcCenters();
				return (T) (Float) mVCenter.x;
			} else {
				return (T) (Float) mBody.getLinearVelocity().x;
			}
		}
		if (name.equals("Velocity Y")) {
			if (mState == SQUISH_STATE) {
				calcCenters();
				return (T) (Float) mVCenter.y;
			} else {
				return (T) (Float) mBody.getLinearVelocity().y;
			}
		}
		return super.getProp(name);
	}

	/**
	 * Sets a given property
	 * 
	 * @param name
	 *            The name of the property
	 * @param val
	 *            The value
	 */
	@Override
	public void setProp(String name, Object val) {
		float value = Convert.getFloat(val);
		if (name.equals("Player ID") && getLevel().getAssetKey() != null) {

			Fixture f;
			Filter filter = new Filter();
			filter.maskBits = (short) (0xFFFF - (2 << (int) value));
			for (Body b : mParticles) {
				f = b.getFixtureList().get(0);
				f.setFilterData(filter);
			}
			if (mLight != null)
				mLight.setContactFilter((short) (2 << (int) value), (short) 0, filter.maskBits);
			filter = new Filter();
			filter.categoryBits = (short) (2 << (int) value);
			mLeftEye.getFixtureList().get(0).setFilterData(filter);
			mRightEye.getFixtureList().get(0).setFilterData(filter);
			mEyeFilter = filter;
			resetColor((int) value, true);
		}
		if (name.equals("Active")) {
			if (mLight != null)
				mLight.setActive(value != 0);
		}
		if (name.equals("Velocity X")) {
			float yVel = mBody.getLinearVelocity().y;
			for (Body b : mParticles) {
				b.setLinearVelocity(Convert.getFloat(val), yVel);
			}
		}
		if (name.equals("Velocity Y")) {
			float xVel = mBody.getLinearVelocity().x;
			for (Body b : mParticles) {
				b.setLinearVelocity(xVel, Convert.getFloat(val));
			}
		}
		if (name.equals("Momentum X")) {
			float yVel = mBody.getLinearVelocity().y;
			for (Body b : mParticles) {
				b.setLinearVelocity(Convert.getFloat(val), yVel);
			}
			return;
		}
		if (name.equals("Momentum Y")) {
			float xVel = mBody.getLinearVelocity().x;
			for (Body b : mParticles) {
				b.setLinearVelocity(xVel, Convert.getFloat(val));
			}
			return;
		}
		if (name.equals("X")) {
			mAccAprox = 0.0f;
		}
		if (name.equals("Y")) {
			mAccAprox = 0.0f;
		}
		if (name.equals("Density")) {
			return;
		}
		super.setProp(name, val);
	}

	private FixtureDef makeEyeFixtureDef() {
		// Create a circle shape
		CircleShape circle = new CircleShape();
		circle.setPosition(new Vector2(0, 0));
		circle.setRadius(0.125f);
		// Create a fixtureDef
		FixtureDef fd = new FixtureDef();
		fd.shape = circle;
		fd.density = .2f;
		fd.friction = 0f;
		fd.restitution = 0.0f;
		return fd;
	}

	private FixtureDef makeTriFixtureDef(PolygonShape ps) {

		FixtureDef fd = new FixtureDef();
		fd.shape = ps;
		fd.density = 1.75f;
		fd.friction = 1f;
		fd.filter.maskBits = (short) (0x7FFF - (2 << getmPlayerID()));
		if (mLight != null)
			mLight.setContactFilter((short) (2 << getmPlayerID()), (short) 0, fd.filter.maskBits);
		return fd;
	}

	private PolygonShape makeTriangle() {
		PolygonShape ps = new PolygonShape();
		float boxlength = DEFAULT_RADIUS * (float) Math.tan(Math.PI / NUM_PARTICLES);
		float verts[] = new float[6];
		verts[0] = -boxlength;
		verts[1] = 0f;
		verts[2] = boxlength;
		verts[3] = 0f;
		verts[4] = 0;
		verts[5] = .1f;
		ps.set(verts);
		return ps;
	}

	private void resetColor(int id, boolean force) {
		Color squishColor;
		if (id >= 0) {
			squishColor = colors((int) (id % COLORS.length));
		} else {
			squishColor = colors(0);
		}

		Color solidColor = new Color(squishColor);
		solidColor.mul(.65f, .65f, .65f, 1f);
		Drawable bd = null;
		for (Drawable d : ((CompositeDrawable) mDrawable).mDrawables) {
			if (d instanceof BlobDrawable) {
				bd = d;
				break;
			}
		}
		if (bd != null) {
			if (force) {
				((BlobDrawable) bd).mCurrentColor = new Color(squishColor);
				if (mLight != null) {
					mLightColor = new Color(0, 0, 0, 0);
					mLightColor.set(mLightColor);
					mLight.setColor(mLightColor);
				}
			}
			((BlobDrawable) bd).mDestColor = new Color(squishColor);
			((BlobDrawable) bd).mSolidColor = solidColor;
			((BlobDrawable) bd).mSquishColor = squishColor;
		}
	}

	public void transform() {
		AssetManager man = Game.get().getAssetManager();
		String[] skips = new String[5];
		skips[0] = "earth";
		skips[1] = "opening";
		skips[2] = "opening_med";
		skips[3] = "opening_hard";
		skips[4] = "closing";
		for (int i = 0; i < skips.length; i++) {
			if (skips[i].equals(Game.get().getLevel().getAssetKey()))
				return;
		}

		Sound sound;
		long soundID;
		if (mState == SQUISH_STATE) {

			Iterable<StableContact> contacts = Game.get().getLevel().getContactHandler()
					.getContacts(this);
			Iterable<Body> bodies = ContactHandler.getBodies(contacts);

			// Already in squish state, transform to solid state.
			mState = SOLID_STATE;

			if (man.isLoaded(mMow)) {
				sound = man.get(mMow, Sound.class);
				soundID = sound.play();
				sound.setPitch(soundID, .5f);
				sound.setVolume(soundID, 1f);
				mSoundTimer.reset();
			}

			calcCenters();

			// Create a BodyDef
			BodyDef bd = new BodyDef();
			bd.position.set(mCenterOfMass.x, mCenterOfMass.y);
			bd.type = BodyType.DynamicBody;
			bd.linearDamping = .1f;
			bd.bullet = true;

			// Create the "main" body
			Body tempBody = getLevel().getWorld().createBody(bd);
			tempBody.setLinearVelocity(mVCenter);

			// Set angular velocity
			float momInertia = 0; // Moment of inertia (from Wikipedia: Moment
			// of Inertia)
			float angMom = 0; // Angular momentum
			for (Body b : mParticles) {
				Vector2 r = b.getPosition().cpy(); // Radial arm
				r.sub(mCenterOfMass);
				momInertia += b.getMass() * r.len2();
				Vector2 linMom = b.getLinearVelocity().cpy(); // Linear momentum
				linMom.scl(b.getMass());
				angMom += r.crs(linMom); // Definition of angular momentum
			}
			float angVel = angMom / momInertia; // Def'n of angular velocity
			tempBody.setAngularVelocity(angVel);

			// Create a fixtureDef
			FixtureDef fd;
			PolygonShape triangle;

			Vector2[] verts = new Vector2[3];
			verts[0] = new Vector2();
			verts[1] = new Vector2();
			verts[2] = new Vector2();
			Vector2 offset;
			float angle;
			for (Body b : mParticles) {
				triangle = makeTriangle();
				fd = makeTriFixtureDef(triangle);
				fd.density *= SOLID_MASS_MULT;
				offset = b.getPosition().sub(mCenterOfMass);
				triangle.getVertex(0, verts[0]);
				triangle.getVertex(1, verts[1]);
				triangle.getVertex(2, verts[2]);
				angle = (float) (b.getAngle() * 180 / Math.PI);
				verts[0].rotate(angle);
				verts[1].rotate(angle);
				verts[2].rotate(angle);
				verts[0].add(offset);
				verts[1].add(offset);
				verts[2].add(offset);
				triangle.set(verts);
				tempBody.createFixture(fd).setUserData(new ArrayList<Blob>());

			}

			ContactHandler contactHandler = getLevel().getContactHandler();
			contactHandler.destroyContacts(mBody);
			contactHandler.destroyContacts(mLeftEye);
			contactHandler.destroyContacts(mRightEye);
			if (mBody != null) {
				getLevel().addBodiesToDestroy(mBody);
			}

			for (Body other : bodies) {
				if (other.getUserData() instanceof Actor) {
					Actor otherActor = (Actor) other.getUserData();
					if (Convert.getInt(otherActor.getProp("Grabbable")) == 1) {
						WeldJointDef wjd = new WeldJointDef();
						wjd.initialize(tempBody, otherActor.mBody, otherActor.mBody.getPosition());
						Joint j = getLevel().getWorld().createJoint(wjd);
						mJoints.add(j);
						if (otherActor instanceof Blob) {
							((Blob) otherActor).giveJoint(j);
						}
						if (otherActor instanceof BattleBall) {
							((BattleBall) otherActor).setProp("Grabbers", (Integer) (Convert
									.getInt(((BattleBall) otherActor).getProp("Grabbers")) + 1));
						}
						mGrabbing = true;
					}
				}
			}

			tempBody.setUserData(this);
			mBody = tempBody;
			for (Body b : mParticles) {
				contactHandler.destroyContacts(b);
				b.setActive(false);
			}
			mBody.applyAngularImpulse(.01f, true);

			setCollisionGroup((Integer) getProp("Collision Group")); // Since we
			// create
			// a new
			// Body,
			// reset
			// Collision
			// Group.
			setProp("Grabbable", (Integer) 1);
			mStrain = true;
		} else if (mState == SOLID_STATE) {

			// Already in solid state, transform to squish state.
			mState = SQUISH_STATE;

			if (man.isLoaded(mMow)) {
				sound = man.get(mMow, Sound.class);
				soundID = sound.play();
				sound.setVolume(soundID, 1f);
				mSoundTimer.reset();
			}

			// Get physics params
			Vector2 linVel = mBody.getLinearVelocity(); // Linear velocity
			float angVel = mBody.getAngularVelocity(); // Angular velocity
			float momInert = mBody.getInertia(); // Moment of inertia
			float angMom = angVel * momInert; // Angular momentum
			float bodyRot = mBody.getAngle() * (float) (180f / Math.PI); // Amount
			// to
			// rotate
			// each
			// shape
			// to
			// get
			// its
			// actual
			// position

			angMom /= mParticles.size(); // Give each particle the same portion
			// of the angular momentum
			calcCenters();
			float angle;
			for (Body b : mParticles) {

				Vector2 pos = b.getPosition();
				angle = b.getAngle() + mBody.getAngle();
				pos.sub(mCenterOfMass);
				pos.rotate(bodyRot);
				pos.add(mBody.getPosition());
				b.setTransform(pos, angle);
				b.setAngularVelocity(0);
				// Get particle velocity due to linear momentum
				Vector2 partVel = linVel.cpy();

				// Get particle velocity due to angular momentum
				Vector2 r = b.getPosition(); // Radial arm from center of mass
				r.sub(mBody.getWorldCenter());

				Vector2 angPortion = r.cpy(); // Angular portion of linear
				// velocity
				angPortion.rotate(90); // (rotate r counterclocwise 90 deg)

				// Adjust length to that that makes the sum of the particles'
				// angular momentums
				// the total angular momentum
				angPortion.nor();
				angPortion.scl(angMom / r.len() / b.getMass() / SOLID_MASS_MULT);
				partVel.add(angPortion);
				// Set total velocity for particle
				b.setLinearVelocity(partVel);
				b.setActive(true);
			}
			Actor otherActor;
			Body body;
			for (Joint j : mJoints) {
				body = (j.getBodyA().equals(mBody) ? j.getBodyB() : j.getBodyA());
				if (j.getBodyA() != null && j.getBodyB() != null) {
					otherActor = (Actor) body.getUserData();
					if (otherActor instanceof Blob) {
						((Blob) otherActor).removeJoint(j);
					}
					if (otherActor instanceof BattleBall) {
						((BattleBall) otherActor).setProp("Grabbers", (Integer) (Convert
								.getInt(((BattleBall) otherActor).getProp("Grabbers")) - 1));
					}
					getLevel().getWorld().destroyJoint(j);
				}
			}
			mJoints = new ArrayList<Joint>();
			ContactHandler contactHandler = getLevel().getContactHandler();
			contactHandler.destroyContacts(mBody);
			contactHandler.destroyContacts(mLeftEye);
			contactHandler.destroyContacts(mRightEye);
			for (Body b : mParticles) {
				contactHandler.destroyContacts(b);
			}
			mBody.setActive(false);
			setProp("Grabbable", (Integer) 0);

			calcCenters();
		}
	}

	public void setPulling(boolean pulled) {
		mGettingPulled = pulled;
	}

	public int getDirection() {
		return mDirection;
	}

	public void removeJoint(Joint j) {
		mJoints.remove(j);
	}

	public void giveJoint(Joint j) {
		mJoints.add(j);
	}

	public ArrayList<Joint> getJoints() {
		return mJoints;
	}

	public Filter getEyeFilter() {
		return mEyeFilter;
	}

	/*
	 * Makes the blob's body
	 */
	private void makeBlobBody() {
		// Create a BodyDef
		BodyDef bd = new BodyDef();
		bd.position.set(0, 0);
		bd.type = BodyType.DynamicBody;
		bd.linearDamping = .1f;
		bd.bullet = true;

		// Create the "main" body
		mBody = getLevel().getWorld().createBody(bd);

		BodyDef eyebd = new BodyDef();
		eyebd.position.set(0, 0);
		eyebd.type = BodyType.DynamicBody;
		eyebd.linearDamping = 30f;
		eyebd.bullet = true;
		mLeftEye = getLevel().getWorld().createBody(eyebd);
		mRightEye = getLevel().getWorld().createBody(eyebd);

		float halfSideLength = DEFAULT_RADIUS * (float) Math.tan(Math.PI / NUM_PARTICLES);
		FixtureDef fdside = makeTriFixtureDef(makeTriangle());

		CircleShape cs = new CircleShape();

		mBody.createFixture(cs, 1.0f).setUserData(new ArrayList<Blob>());
		;
		mBody.setActive(false);
		mBody.setUserData(this);
		FixtureDef eyefd = makeEyeFixtureDef();
		mLeftEye.createFixture(eyefd).setUserData(new ArrayList<Blob>());
		;
		mLeftEye.setFixedRotation(true);
		mLeftEye.setUserData(this);
		mLeftEye.setGravityScale(0);
		eyefd = makeEyeFixtureDef();
		mRightEye.createFixture(eyefd).setUserData(new ArrayList<Blob>());
		;
		mRightEye.setFixedRotation(true);
		mRightEye.setUserData(this);
		mRightEye.setGravityScale(0);
		mSubBodies.add(mRightEye);
		mSubBodies.add(mLeftEye);
		Body pBody;
		// Create the bodies and fixtures for each of the particles
		for (int i = 0; i < NUM_PARTICLES; i++) {
			pBody = getLevel().getWorld().createBody(bd); // Body of a particle
			pBody.setUserData(this);
			double angle = Math.PI * 2 / NUM_PARTICLES * i;
			float x = (float) Math.cos(angle) * DEFAULT_RADIUS;
			float y = (float) Math.sin(angle) * DEFAULT_RADIUS;
			pBody.createFixture(fdside).setUserData(new ArrayList<Blob>());
			;
			angle += Math.PI / 2;
			pBody.setTransform(new Vector2(x, y), (float) angle);
			pBody.setBullet(true);
			mParticles.add(pBody);
			mSubBodies.add(pBody);
		}
		for (int i = 0; i < NUM_PARTICLES; i++) {
			RevoluteJointDef rjd = new RevoluteJointDef();
			double angle = Math.PI * 2 / NUM_PARTICLES * i;
			float x = (float) Math.cos(angle) * DEFAULT_RADIUS;
			float y = (float) Math.sin(angle) * DEFAULT_RADIUS;
			Vector2 jointOffset = new Vector2(x, y);
			jointOffset.nor();
			jointOffset.rotate(90);
			jointOffset.scl(halfSideLength);
			jointOffset.add(new Vector2(x, y));
			rjd.initialize(mParticles.get((i + 1) % NUM_PARTICLES), mParticles.get(i), jointOffset);
			rjd.collideConnected = false;
			if (getLevel() != null) {
				getLevel().getWorld().createJoint(rjd);
			}
		}

		// Create the springs
		for (int i = 0; i < NUM_PARTICLES; i++) {
			for (int j = i + 1; j < NUM_PARTICLES; j++) {
				Spring s = new Spring();
				s.a = i;
				s.b = j;
				Body a = mParticles.get(i);
				Body b = mParticles.get(j);
				float dx = a.getPosition().x - b.getPosition().x;
				float dy = a.getPosition().y - b.getPosition().y;
				s.restLength = (float) Math.sqrt(dx * dx + dy * dy);

				s.elasticity = .18f;
				if (s.restLength < DEFAULT_RADIUS) {
					s.elasticity += 15f / (s.restLength);
				}

				if (s.elasticity > 10) {
					s.elasticity = 10f;
				}
				mSprings.add(s);
			}
		}

		// Give Body a reference to its Actor
		mBody.setUserData(this);
	}

	@Override
	public void left() {

	}

	@Override
	public void right() {
		// TODO Auto-generated method stub

	}

	@Override
	public void up() {
		// TODO Auto-generated method stub

	}

	@Override
	public void down() {
		// TODO Auto-generated method stub

	}

	@Override
	public void triggerLeft() {
		// TODO Auto-generated method stub

	}

	@Override
	public void triggerRight() {
		// TODO TRIGGER TO TRANSFORM FLICKERS
	}

	@Override
	public void downAction(int id) {
		/*
		 * if (getLevel().getAssetKey() != null) { if (Game.get().getState() ==
		 * Game.PLAY || Game.get().getState() == Game.MENU) { if (id ==
		 * ControllerFilterAPI.BUTTON_A && mState == SOLID_STATE) { transform();
		 * } else if (id == ControllerFilterAPI.BUTTON_B) { transform(); } } }
		 */
	}

	@Override
	public void upAction(int id) {

	}

	@Override
	public void dpad(int id, PovDirection dir) {
		/*
		 * if (getLevel().getAssetKey() != null) { if (Game.get().getState() ==
		 * Game.PLAY || Game.get().getState() == Game.MENU) { boolean up = dir
		 * == PovDirection.north; boolean down = dir == PovDirection.south; if
		 * (up && mState == SOLID_STATE) { transform(); } else if (down) {
		 * transform(); } } }
		 */
	}

	@Override
	public void postLoad() {
		if (getLevel().getAssetKey() == null)
			return;
		int difficulty = Convert.getInt(Game.get().getLevel().getProp("Difficulty"));
		if (difficulty == 0) {
			mBlobDrawable.mAccessoryHat = mBlobDrawable.mHats.get(0);
		} else if (difficulty == 1) {
			mBlobDrawable.mAccessoryHat = mBlobDrawable.mHats.get(1);
		} else if (difficulty == 2) {
			mBlobDrawable.mAccessoryHat = mBlobDrawable.mHats.get(2);
		}
	}

	/**
	 * Called to detect if any joints are strained to the breaking point. These
	 * joints are then removed.
	 */

	private boolean mStrain = false;

	private void breakStrainedJoints() {

		for (Iterator<Joint> iter = mJoints.iterator(); iter.hasNext();) {
			Joint j = iter.next();

			// Ignore joint if it isn't breaking
			Vector2 force = j.getReactionForce(60);
			if (force.len() < GRAB_BREAK_FORCE) {
				continue;
			}

			// Remove joint
			if (!mStrain) {
				Actor otherActor;
				Body body;
				body = (j.getBodyA().equals(mBody) ? j.getBodyB() : j.getBodyA());
				if (j.getBodyA() != null && j.getBodyB() != null) {
					otherActor = (Actor) body.getUserData();
					if (otherActor instanceof Blob) {
						((Blob) otherActor).removeJoint(j);
					}
					if (otherActor instanceof BattleBall) {
						((BattleBall) otherActor).setProp("Grabbers", (Integer) (Convert
								.getInt(((BattleBall) otherActor).getProp("Grabbers")) - 1));
					}
					getLevel().getWorld().destroyJoint(j);
					Game.get().playTickSound();
					iter.remove();
				}
			}
			mStrain = false;

		}
	}

	// /////////////
	// Properties
	// /////////////

	@Override
	public float getX() {
		if (mState == SQUISH_STATE) {
			calcCenters();
			return mCenterOfMass.x;
		} else {
			return mBody.getPosition().x;
		}
	}

	@Override
	public void setX(float x) {
		float xpos;
		if (mState == SQUISH_STATE && mCenterOfMass != null) {
			xpos = mCenterOfMass.x;
		} else {
			xpos = mBody.getPosition().x;
		}
		float offset = x - xpos; // How much the main body was shifted
		mBody.setTransform(x, mBody.getPosition().y, mBody.getAngle());
		mBody.setLinearVelocity(0, 0);

		mLeftEye.setTransform(x - mEyeOffset.x, mBody.getPosition().y + mEyeOffset.y, 0);
		mRightEye.setTransform(x + mEyeOffset.x, mBody.getPosition().y + mEyeOffset.y, 0);
		mLeftEyeDest = mLeftEye.getPosition().cpy();
		mRightEyeDest = mRightEye.getPosition().cpy();

		// Shift all the particles by the same amount
		for (Body b : mParticles) {
			b.setTransform(b.getPosition().x + offset, b.getPosition().y, b.getAngle());
		}
		calcCenters();
		return;
	}

	@Override
	public float getY() {
		if (mState == SQUISH_STATE) {
			calcCenters();
			return mCenterOfMass.y;
		} else {
			return mBody.getPosition().y;
		}
	}

	@Override
	public void setY(float y) {
		float ypos;
		if (mState == SQUISH_STATE && mCenterOfMass != null) {
			ypos = mCenterOfMass.y;
		} else {
			ypos = mBody.getPosition().y;
		}
		float offset = y - ypos; // How much the main body was shifted
		mBody.setTransform(mBody.getPosition().x, y, mBody.getAngle());
		mBody.setLinearVelocity(0, 0);
		mLeftEye.setTransform(mBody.getPosition().x - mEyeOffset.x, y + mEyeOffset.y, 0);
		mRightEye.setTransform(mBody.getPosition().x + mEyeOffset.x, y + mEyeOffset.y, 0);
		mLeftEyeDest = mLeftEye.getPosition().cpy();
		mRightEyeDest = mRightEye.getPosition().cpy();
		// Shift all the particles by the same amount
		for (Body b : mParticles) {
			b.setTransform(b.getPosition().x, b.getPosition().y + offset, b.getAngle());
		}
		calcCenters();
		return;
	}

	@Override
	public float getVelocityX() {
		if (mState == SQUISH_STATE) {
			calcCenters();
			return mVCenter.x;
		} else {
			return mBody.getLinearVelocity().x;
		}
	}

	@Override
	public void setVelocityX(float m) {
		float yVel = mBody.getLinearVelocity().y;
		for (Body b : mParticles) {
			b.setLinearVelocity(m, yVel);
		}
		super.setVelocityX(m);
	}

	@Override
	public float getVelocityY() {
		if (mState == SQUISH_STATE) {
			calcCenters();
			return mVCenter.y;
		} else {
			return mBody.getLinearVelocity().y;
		}
	}

	@Override
	public void setVelocityY(float m) {
		float xVel = mBody.getLinearVelocity().x;
		for (Body b : mParticles) {
			b.setLinearVelocity(xVel, m);
		}
		super.setVelocityY(m);
	}
}
