package com.example.hometask.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Custom View that creates a flowing particle animation effect.
 */
public class DataFlowAnimationView extends View {

    private List<Particle> particles;
    private Paint paint;
    private Random random;
    private int width, height;
    private ValueAnimator animator;

    /**
     * Constructor for the DataFlowAnimationView.
     * @param context The Context the view is running in
     * @param attrs The attributes of the XML tag that is inflating the view
     */
    public DataFlowAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initializes the view's properties.
     */
    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        random = new Random();
        particles = new ArrayList<>();
    }

    /**
     * Called when the size of this view has changed.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        initParticles();
        startAnimation();
    }

    /**
     * Initializes the particles for the animation.
     */
    private void initParticles() {
        particles.clear();
        for (int i = 0; i < 50; i++) {
            particles.add(createParticle());
        }
    }

    /**
     * Creates a single particle with random properties.
     * @return A new Particle object
     */
    private Particle createParticle() {
        float x = random.nextFloat() * width;
        float y = height + random.nextFloat() * height; // Start below the screen
        float speed = 2f + random.nextFloat() * 2f;
        float size = 5f + random.nextFloat() * 10f;
        // Generate shades of green
        int color = Color.argb(150 + random.nextInt(105),
                100 + random.nextInt(55), 200 + random.nextInt(55), 0);
        return new Particle(x, y, speed, size, color);
    }

    /**
     * Starts the animation of the particles.
     */
    private void startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(16); // 60 FPS
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            updateParticles();
            invalidate();
        });
        animator.start();
    }

    /**
     * Updates the positions of the particles.
     */
    private void updateParticles() {
        for (Particle p : particles) {
            p.y -= p.speed;
            if (p.y + p.size < 0) {
                // Particle is off-screen, reset it
                int index = particles.indexOf(p);
                particles.set(index, createParticle());
            }
        }
    }

    /**
     * Draws the particles on the canvas.
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        for (Particle p : particles) {
            paint.setColor(p.color);
            canvas.drawCircle(p.x, p.y, p.size, paint);
        }
    }

    /**
     * Represents a single particle in the animation.
     */
    private static class Particle {
        float x, y, speed, size;
        int color;

        Particle(float x, float y, float speed, float size, int color) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.size = size;
            this.color = color;
        }
    }

    /**
     * Called when the view is detached from the window.
     * Ensures that the animation is properly cleaned up.
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
        }
    }
}