package com.fusit.stitchutils.customui;

import android.transition.Visibility;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;


public class CircularRevealTransition extends Visibility {
    @Override
    public Animator onAppear(ViewGroup sceneRoot, final View view, TransitionValues startValues, TransitionValues endValues) {
        view.setAlpha(0);
        int startRadius = 0;
        int endRadius = (int) Math.hypot(view.getWidth(), view.getHeight());
        Animator reveal = ViewAnimationUtils.createCircularReveal(view, view.getWidth() / 2, view.getHeight() / 2, startRadius, endRadius);
        //make view invisible until animation actually starts
//        view.setAlpha(0);
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setAlpha(1);
            }
        });
        return reveal;
    }

    @Override
    public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        int endRadius = 0;
        int startRadius = (int) Math.hypot(view.getWidth(), view.getHeight());
//        Animator reveal = ViewAnimationUtils.createCircularReveal(view, view.getWidth() / 2, view.getHeight() / 2, startRadius, endRadius);
        Animator reveal = ViewAnimationUtils.createCircularReveal(view, view.getWidth() / 2, view.getHeight() / 2, startRadius, endRadius);
        return reveal;
    }
}
