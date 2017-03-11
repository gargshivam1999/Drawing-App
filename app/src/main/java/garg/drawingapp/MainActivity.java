package garg.drawingapp;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;


public class MainActivity extends AppCompatActivity {

    private DrawingView drawView;
    private DrawingType dType;
    private float smallBrush, mediumBrush, largeBrush;
    private String fileName;
    private boolean isSaved;

    enum DrawingType {
        Rectangle,
        RoundRectangle,
        Eclipse,
        Brush,
        Line
    }

    ImageButton newDrawing, saveDrawing, erase, rectangle, roundRectangle, eclipse, brush, line, colorPalette;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isSaved = false;

        dType = DrawingType.Line;
        drawView = (DrawingView) findViewById(R.id.drawing);
        drawView.setDrawingType(dType);

        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        newDrawing = (ImageButton) findViewById(R.id.new_drawing);
        saveDrawing = (ImageButton) findViewById(R.id.save_drawing);
        erase = (ImageButton) findViewById(R.id.erase);
        rectangle = (ImageButton) findViewById(R.id.rectangle);
        roundRectangle = (ImageButton) findViewById(R.id.round_rectangle);
        eclipse = (ImageButton) findViewById(R.id.eclipse);
        brush = (ImageButton) findViewById(R.id.brush);
        line = (ImageButton) findViewById(R.id.line);
        colorPalette = (ImageButton) findViewById(R.id.color_palette);


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            roundRectangle.setVisibility(View.GONE);
            eclipse.setVisibility(View.GONE);
        }

        newDrawing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder newDialog = new AlertDialog.Builder(MainActivity.this);
                newDialog.setTitle("New drawing");
                newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
                newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        drawView.startNew();
                        isSaved = false;
                        dialog.dismiss();
                    }
                });
                newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                newDialog.show();
            }
        });

        saveDrawing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDrawing();
            }

        });

        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(true);
                isSaved = false;
                setBrushSize();
            }
        });

        rectangle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(false);
                drawView.setBrushSize(5.f);
                isSaved = false;
                drawView.setDrawingType(DrawingType.Rectangle);
            }
        });

        roundRectangle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(false);
                drawView.setBrushSize(5.f);
                isSaved = false;
                drawView.setDrawingType(DrawingType.RoundRectangle);
            }
        });

        eclipse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(false);
                isSaved = false;
                drawView.setDrawingType(DrawingType.Eclipse);
            }
        });


        brush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(false);
                isSaved = false;
                setBrushSize();
            }
        });

        line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(false);
                drawView.setBrushSize(5.f);
                isSaved = false;
                drawView.setDrawingType(DrawingType.Line);
            }
        });

        colorPalette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (!isSaved) {
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(MainActivity.this);
            saveDialog.setTitle("Do you want to save the Drawing?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    saveDrawing();
                }
            });
            saveDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            saveDialog.show();
        } else super.onBackPressed();
    }

    private void saveDrawing() {

        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != 0) ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
        if (permissionCheck == 0) {
            isSaved = true;
            makeFolder();
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(MainActivity.this);
            saveDialog.setTitle("Name of Drawing");
            final EditText input = new EditText(this);
            saveDialog.setView(input);
            saveDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    fileName = input.getText().toString().trim();
                    Bitmap bitmap = drawView.saveDrawing();
                    File file = new File(Environment.getExternalStorageDirectory() + "/Drawing/" + fileName + ".png");
                    try {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
                        Toast savedToast = Toast.makeText(getApplicationContext(),
                                "Drawing saved to Folder Drawing!", Toast.LENGTH_SHORT);
                        savedToast.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            saveDialog.show();
        } else {
            Toast.makeText(MainActivity.this, "This app does not have permission to save image.", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeFolder() {
        File f = new File(Environment.getExternalStorageDirectory(), "Drawing");
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    private void setBrushSize() {
        final Dialog brushDialog = new Dialog(MainActivity.this);
        brushDialog.setTitle("Brush size:");
        brushDialog.setContentView(R.layout.brush_chooser);
        ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
        smallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setBrushSize(smallBrush);
                drawView.setDrawingType(DrawingType.Brush);
                brushDialog.dismiss();
            }
        });
        ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
        mediumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setBrushSize(mediumBrush);
                drawView.setDrawingType(DrawingType.Brush);
                brushDialog.dismiss();
            }
        });
        ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
        largeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setBrushSize(largeBrush);
                drawView.setDrawingType(DrawingType.Brush);
                brushDialog.dismiss();
            }
        });
        Button cancelBtn = (Button) brushDialog.findViewById(R.id.cancel_action);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(false);
                brushDialog.dismiss();
            }
        });
        brushDialog.setCancelable(false);
        brushDialog.show();
    }

    private void show() {
        GridView gv = (GridView) ColorPicker.getColorPicker(MainActivity.this);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(gv);

        final AlertDialog dialog = builder.create();
        dialog.show();

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int mPickedColor = (int) parent.getItemAtPosition(position);
                colorPalette.setColorFilter(mPickedColor);
                drawView.setColor(mPickedColor);
                dialog.dismiss();
            }
        });
    }
}
