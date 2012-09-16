/**
 * Copyright (c) 2011, JFXtras
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jfxtras.labs.scene.control;


import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradientBuilder;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.SVGPathBuilder;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.ScaleBuilder;
import static jfxtras.labs.scene.control.SlideLock.PREFERRED_WIDTH;
import static jfxtras.labs.scene.control.SlideLock.PREFERRED_HEIGHT;

/**
 * This represents the actual drawing of the slide to unlock control. All mouse and touch event handlers are
 * managed in this class.
 *
 * @author cdea
 */
public class SlideLockSkin extends Region implements Skin<SlideLock>{
    private final SlideLock          CONTROL;
    private Group                    button;
    private Text                     text;
    private EventHandler<MouseEvent> mouseHandler;
    private EventHandler<TouchEvent> touchHandler;

    
    public SlideLockSkin(final SlideLock slideLock) {
        this.CONTROL = slideLock;

        if (CONTROL.getPrefWidth() <= 0 || CONTROL.getPrefHeight() <= 0) {
            CONTROL.setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        }

        button = new Group();
        text   = new Text(CONTROL.getText());

        mouseHandler = new EventHandler<MouseEvent>() {
            @Override public void handle(final MouseEvent event) {
                if (MouseEvent.MOUSE_PRESSED == event.getEventType()) {
                    buttonPressAction(button, event.getX(), event.getY());
                } else if (MouseEvent.MOUSE_DRAGGED == event.getEventType()) {
                    moveButtonAction(button, event.getX(), event.getY());
                } else if (MouseEvent.MOUSE_RELEASED == event.getEventType()) {
                    buttonSnapBack();
                }
            }
        };

        touchHandler = new EventHandler<TouchEvent>() {
            @Override public void handle(final TouchEvent event) {
                if (TouchEvent.TOUCH_PRESSED == event.getEventType()) {
                    buttonPressAction(button, event.getTouchPoint().getX(), event.getTouchPoint().getY());
                } else if (TouchEvent.TOUCH_MOVED == event.getEventType()) {
                    moveButtonAction(button, event.getTouchPoint().getX(), event.getTouchPoint().getY());
                } else if (TouchEvent.TOUCH_RELEASED == event.getEventType()) {
                    buttonSnapBack();
                }
            }
        };

        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        drawControl();
        addHandlers();
    }

    @Override public void layoutChildren() {
        drawControl();
        super.layoutChildren();
    }

    @Override public SlideLock getSkinnable() {
        return CONTROL;
    }

    @Override public Node getNode() {
        return this;
    }

    @Override public void dispose() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void addHandlers() {
        // MouseEvents
        setOnMousePressed(mouseHandler);
        setOnMouseDragged(mouseHandler);
        setOnMouseReleased(mouseHandler);
        // TouchEvents
        setOnTouchPressed(touchHandler);
        setOnTouchMoved(touchHandler);
        setOnTouchReleased(touchHandler);
    }

    private void buttonPressAction(final Node slideButton, final double X, final double Y) {
        if (slideButton.getBoundsInParent().contains(X, Y)) {
            CONTROL.setStartX(X - CONTROL.getEndX());
        }
    }

    private void moveButtonAction(final Node slideButton, final double X, final double Y) {
        final double WIDTH          = CONTROL.getPrefWidth();
        final double HEIGHT         = CONTROL.getPrefHeight();
        final double SCALE_FACTOR_X = WIDTH / PREFERRED_WIDTH;
        final double SCALE_FACTOR_Y = HEIGHT / PREFERRED_HEIGHT;

        System.out.println(X + ", " + Y);

        // check when user pressed the button
        if (slideButton.getBoundsInParent().contains(X, Y)) {
            CONTROL.setEndX(X - CONTROL.getStartX());
            if (slideButton.getTranslateX() < SlideLock.START_XCOORD) {
                CONTROL.setLocked(true);
                CONTROL.setEndX(SlideLock.START_XCOORD);
            } else if (slideButton.getTranslateX() > SlideLock.END_XCOORD) {
                CONTROL.setLocked(false);
                CONTROL.setEndX(SlideLock.END_XCOORD);
            } else {
                CONTROL.setLocked(true);
            }
            double opacity = 1d - (slideButton.getTranslateX()) / 200.0;
            if (opacity < 0) {
                opacity = 0;
            }
            CONTROL.setTextOpacity(opacity);
        }
    }


    // ******************** Drawing related ***********************************
    private void buttonSnapBack() {
        if (CONTROL.isLocked()){
            CONTROL.getSnapButtonBackAnim().play();
        }
    }

    private void drawControl() {
        final double WIDTH          = CONTROL.getPrefWidth();
        final double HEIGHT         = CONTROL.getPrefHeight();
        final double SCALE_FACTOR_X = WIDTH / PREFERRED_WIDTH;
        final double SCALE_FACTOR_Y = HEIGHT / PREFERRED_HEIGHT;
        final Scale SCALE           = ScaleBuilder.create()
                .x(WIDTH / PREFERRED_WIDTH)
                .y(HEIGHT / PREFERRED_HEIGHT)
                .pivotX(0)
                .pivotY(0)
                .build();

        Rectangle backgroundRect = RectangleBuilder.create()
                .id("slide-background")
                .width(WIDTH)
                .height(HEIGHT)
                .build();
        backgroundRect.visibleProperty().bind(CONTROL.backgroundVisibleProperty());

        Rectangle slideArea = RectangleBuilder.create()
                .id("slide-area")
                .x(0.0612476117 * WIDTH)
                .y(0.2463297872 * HEIGHT)
                .width(0.8829200973 * WIDTH)
                .height(0.5319148936 * HEIGHT)
                .arcWidth(0.079787234 * HEIGHT)
                .arcHeight(0.079787234 * HEIGHT)
                .build();

        SVGPath glareRect = SVGPathBuilder.create()
                .fill(LinearGradientBuilder.create()
                        .proportional(true)
                        .startX(0)
                        .startY(0)
                        .endX(0)
                        .endY(1)
                        .stops(new Stop(0, Color.web("f0f0f0", 1)),
                                new Stop(1, Color.web("f0f0f0", 0))
                        )
                        .build()
                )
                .opacity(.274)
                .transforms(SCALE)
                .content("m 0,0 0,94 32,0 0,-27.218747 C 30.998808,55.222973 37.761737,45.9354 46.156457,45.93665 l 431.687503,0.06427 c 8.39472,0.0013 15.15487,9.290837 15.15315,20.814756 l -0.004,27.218754 30.28125,0 0,-94.0000031 L 0,0 z")
                .id("glare-frame")
                .build();
        glareRect.visibleProperty().bind(CONTROL.backgroundVisibleProperty());

        text.setText(CONTROL.getText());
        text.setId("slide-text");
        text.getTransforms().clear();
        text.getTransforms().add(SCALE);

        drawSlideButton();
        button.translateXProperty().bind(CONTROL.endXProperty().multiply(SCALE_FACTOR_X));
        button.setTranslateY(SlideLock.BUTTON_YCOORD * SCALE_FACTOR_Y);

        text.setTranslateX(SlideLock.START_XCOORD + button.getBoundsInParent().getWidth() + 0.1063829787 * HEIGHT);
        text.setTranslateY(0.5744680851 * HEIGHT);
        text.opacityProperty().bind(CONTROL.textOpacityProperty());

        Rectangle topGlareRect = RectangleBuilder.create()
                .id("slide-top-glare")
                .fill(Color.WHITE)
                .width(WIDTH)
                .height(0.5 * HEIGHT)
                .opacity(0.0627451)
                .build();
        topGlareRect.visibleProperty().bind(CONTROL.backgroundVisibleProperty());
        getChildren().clear();
        getChildren().addAll(backgroundRect, slideArea, glareRect, text, button, topGlareRect);
    }

    private void drawSlideButton() {
        final double WIDTH   = CONTROL.getPrefWidth();
        final double HEIGHT  = CONTROL.getPrefHeight();
        final double SCALE_X = WIDTH / PREFERRED_WIDTH;
        final double SCALE_Y = HEIGHT / PREFERRED_HEIGHT;
        Scale scale = new Scale();
        scale.setX(SCALE_X);
        scale.setY(SCALE_Y);
        scale.setPivotX(0);
        scale.setPivotY(0);

        button.getChildren().clear();

        // build gradientRect
        Rectangle gradientRect = RectangleBuilder.create()
                .x(0.0358943492 * WIDTH)
                .y(0.0649521277 * HEIGHT)
                .width(0.156464544 * WIDTH)
                .height(0.3616446809 * HEIGHT)
                .arcWidth(0.0929042553 * HEIGHT)
                .arcHeight(0.0929042553 * HEIGHT)
                        //.fill(Color.WHITE)
                .fill(CONTROL.getButtonArrowBackgroundColor())
                .id("button-gradient-rect")
                .build();
        button.getChildren().add(gradientRect);

        // build arrowBlurShadow
        SVGPath arrowBlurShadow = SVGPathBuilder.create()
                .fill(Color.BLACK)
                .effect(new GaussianBlur(5))
                .transforms(scale)
                .content("m 17.40912,2.47162 c -8.27303,0 -14.9375,7.04253 -14.9375,15.78125 l 0,59.9375 c 0,8.73872 6.66447,15.75 14.9375,15.75 l 84.625,0 c 8.27303,0 14.9375,-7.01128 14.9375,-15.75 l 0,-59.9375 c 0,-8.73872 -6.66447,-15.78125 -14.9375,-15.78125 l -84.625,0 z m 45.0625,18.15625 27.5625,27.59375 -27.5625,27.5625 0,-15.5625 -33.0625,0 0,-24 33.0625,0 0,-15.59375 z")
                .id("#button-arrow-blur-shadow")
                .build();
        button.getChildren().add(arrowBlurShadow);

        // build arrowStencilCrisp
        SVGPath arrowStencilCrisp = SVGPathBuilder.create()
                .content("m 17.40912,0.47162 c -8.27303,0 -14.9375,7.04253 -14.9375,15.78125 l 0,59.9375 c 0,8.73872 6.66447,15.75 14.9375,15.75 l 84.625,0 c 8.27303,0 14.9375,-7.01128 14.9375,-15.75 l 0,-59.9375 c 0,-8.73872 -6.66447,-15.78125 -14.9375,-15.78125 l -84.625,0 z m 45.0625,18.15625 27.5625,27.59375 -27.5625,27.5625 0,-15.5625 -33.0625,0 0,-24 33.0625,0 0,-15.59375 z")
                .fill(CONTROL.getButtonColor())
                .id("#button-arrow-stencil-crisp")
                .transforms(scale)
                .build();
        button.getChildren().add(arrowStencilCrisp);

        // build glareRect
        SVGPath glareRect = SVGPathBuilder.create()
                .content("m 17.83252,1.67757 c -8.27303,0 -14.9375,7.21042 -14.9375,16.15746 l 0,28.31557 114.5,0 0,-28.31557 c 0,-8.94704 -6.66447,-16.15746 -14.9375,-16.15746 l -84.625,0 z")
                .fill(LinearGradientBuilder.create()
                        .proportional(true)
                        .startX(0)
                        .startY(1)
                        .endX(0)
                        .endY(0)
                        .stops(new Stop(0, Color.web("f4f4f4", 0.60)),
                                new Stop(1, Color.web("ffffff", 0.2063063)))
                        .build()
                )
                .id("#button-arrow-glare-rect")
                .transforms(scale)
                .build();
        glareRect.visibleProperty().bind(CONTROL.buttonGlareVisibleProperty()); // red button
        button.getChildren().add(glareRect);
        button.setCache(true);
    }
}
