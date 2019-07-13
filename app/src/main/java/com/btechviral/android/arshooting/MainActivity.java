package com.btechviral.android.arshooting;

import android.graphics.Point;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.widget.Button;
import android.widget.TextView;

import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private Scene scene;
    private Camera camera;
    private ModelRenderable bulletRenderable;
    private boolean shouldStartTimer = true;
    private int balloonsLeft = 20;
    private Point point;
    private TextView balloonsLeftText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        point = new Point();
        display.getRealSize(point);
        setContentView(R.layout.activity_main);

        CustomArFragment fragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        scene = fragment.getArSceneView().getScene();
        camera = scene.getCamera();

        balloonsLeftText = findViewById(R.id.balloonsCntText);

        addBalloonsToScene();
        buildBulletModel();

        Button button = findViewById(R.id.shootButton);

        button.setOnClickListener(view -> {
            if(shouldStartTimer){
                button.setText("Shoot");
                startTimer();
                shouldStartTimer = false;
            }

            shoot();
            if(balloonsLeft == 0){
                shouldStartTimer = true;
                addBalloonsToScene();
                buildBulletModel();
                balloonsLeft = 20;
                button.setText("Reset");
            }
        });


    }

    private void shoot() {
        Ray ray = camera.screenPointToRay(point.x/2f, point.y/2f);
        Node node = new Node();
        node.setRenderable(bulletRenderable);
        scene.onAddChild(node);

        new Thread(() -> {
           for(int i = 0; i < 200; i++){
               int finalI = i;
               runOnUiThread( () -> {
                   Vector3 vector3 = ray.getPoint(finalI * 0.1f);
                   node.setWorldPosition(vector3);

                   Node nodeInContact = scene.overlapTest(node);

                   if(nodeInContact != null){
                       balloonsLeft--;
                       balloonsLeftText.setText("Balloons left : " + balloonsLeft);
                       scene.removeChild(nodeInContact);
                       scene.removeChild(node);
                   }
               });

               try {
                   Thread.sleep(10);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }

           }

           runOnUiThread(() -> {
               scene.removeChild(node);
           });
        }).start();
    }

    private void startTimer() {
        TextView timerText = findViewById(R.id.timerText);
        new Thread(() -> {
            int seconds = 0;
            while(balloonsLeft > 0){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                seconds ++;
                int minutesPassed = seconds / 60;
                int secondsPassed = seconds % 60;

                runOnUiThread(() -> {
                    timerText.setText( minutesPassed + " : " + secondsPassed );
                });

            }
        }).start();
    }

    private void buildBulletModel() {
        Texture.builder().setSource(this, R.drawable.feature)
                .build()
                .thenAccept(texture -> {
                    MaterialFactory
                            .makeOpaqueWithTexture(this, texture)
                            .thenAccept(material -> {
                                bulletRenderable = ShapeFactory.makeSphere(0.01f, new Vector3(
                                        0f, 0f, 0f
                                ), material);
                            });
                });
    }

    private void addBalloonsToScene() {
        ModelRenderable.builder().
                setSource(this, Uri.parse("Balloon.sfb")).
                build().
                thenAccept(renderable -> {
                    for(int i = 0; i < 20; i++){
                        Node node = new Node();
                        node.setRenderable(renderable);
                        scene.addChild(node);

                        Random random = new Random();

                        final int[] x = {random.nextInt(10)};
                        int y = random.nextInt(20);
                        final int[] z = {random.nextInt(10)};

                        z[0] = -z[0];

                        int finalZ = z[0];
                        new Thread(() -> {
                            z[0] -=4;
                            x[0] += 4;
                            runOnUiThread(() -> {
                                node.setWorldPosition(new Vector3(
                                        (float) x[0],
                                        y/10f,
                                        (float) finalZ
                                ));
                            });
                        }).start();


                    }
                });
    }
}
