package com.example.mydrobe;

import static android.os.Environment.getExternalStoragePublicDirectory;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private static final Logger LOGGER = Logger.getLogger(MainActivity.class.getName());

    private static final int PERMISSION_CODE = 1;
    private Random random = new Random();
    private int modo = 0;
    private List<String> poolNormalSentences;
    private List<String> poolObsceneSentences;
    public final File fichero = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "usuario.bat");
    private Usuario usuario = new Usuario();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeSystem();
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveUser();
    }
    private boolean checkPermissions() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Deseas salis de MYDrove?")
                    .setPositiveButton("Si", (dialogInterface, i) -> {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        saveUser();
                    })
                    .setNegativeButton("Cancelar", (dialogInterface, i) -> dialogInterface.dismiss());
            builder.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void saveUser() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fichero))) {
            oos.writeObject(usuario);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeSystem() { //Cargamos el ususario y las frases en el MainActivity..
        //Cargamos el usuario
        long files = fichero.length();
        boolean x = files == 0;
        if (!x) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fichero))) {
                this.usuario = (Usuario) ois.readObject();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }

        }
        if (!fichero.exists() && checkPermissions()) {
            try {
                fichero.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public List<String> getPoolNormalSentences() {
        return poolNormalSentences;
    }

    /**
     * Cambia la interfaz a la tienda
     * @param view The view of the instance
     */
    public void showTienda(@SuppressWarnings("UnusedParameters") View view) {
        setContentView(R.layout.interfazprincipal);
    }

    public void showAyuda(@SuppressWarnings("UnusedParameters") View view) {
        setContentView(R.layout.show_ayuda);
    }
    /**
     * Cambia la interfaz a la tienda de skins
     *
     * @param view The view of the instance
     */
    public void showSkinsStore(@SuppressWarnings("UnusedParameters") View view) {
        setContentView(R.layout.interfaztiendaskins);
    }

    /**
     * Cambia la interfaz al menu obsceno
     *
     * @param view The view of the instance
     */


    /**
     * Cambia la interfaz al menu normal
     *
     * @param view The view of the instance
     */
    public void showMenu(@SuppressWarnings("UnusedParameters") View view) {
        setContentView(R.layout.activity_main);
    }

    /**
     * Cambia la interfaz a el formulario para crear frases propias
     *
     * @param view The view of the instance
     */
    public void showCrearFrase(@SuppressWarnings("UnusedParameters") View view) {
        setContentView(R.layout.show_ayuda);
    }

    /**
     * Vuelve a la interfaz anterior a tienda
     *
     * @param view The view of the instance
     */
    public void atras(View view) {
            showMenu(view);
    }

    /**
     * Vuelve a la interfaz anterior a tienda skins
     *
     * @param view The view of the instance
     */
    public void atras2(View view) {
        showTienda(view);

    }
    ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        Uri path = Objects.requireNonNull(result.getData()).getData();
                        InputStream image = getContentResolver().openInputStream(path);
                        Bitmap view = BitmapFactory.decodeStream(image);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });

    public void loadImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        activityLauncher.launch(intent);
    }

    // Transforma la image al tipo de file compatible con skin
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                Uri path = Objects.requireNonNull(data).getData();
                InputStream image = getContentResolver().openInputStream(path);
                Bitmap view = BitmapFactory.decodeStream(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Permite al usuario auimentar el numero de puntos obtenidos al hacer click a cambio de una cantidad de puntos
     *
     * @param view The viwe of the instance
     */





    /**
     * Permite al usuario agregar una frase aleatoria a su pool de frases a cambio de una cantidad de puntos
     *
     * @param view The view of the instance
     */
    public void comprarFrase(View view) {
        String frase;
        if (usuario.pago(25)) {
            if (modo == 0) {
                frase = usuario.yaEstaFrase(poolNormalSentences, usuario.getNormalSentencePool());
                usuario.anadirFrase(usuario.getNormalSentencePool(), frase);
            } else {
                frase = usuario.yaEstaFrase(poolObsceneSentences, usuario.getObsceneSentencePool());
                usuario.anadirFrase(usuario.getObsceneSentencePool(), frase);
            }
            if (frase == null) {
                Snackbar mySnackbar = Snackbar.make(view, "Ya has desbloqueado todas las frases", 1000);
                mySnackbar.show();
                usuario.setContador(usuario.getContador() + 30);
            }
        }
    }

    //Establece las frases iniciales.


    /**
     * Permite al usuario reiniciar su progresso a cambio de obtener más puntos al hacer click permanentemente
     *
     * @param view The view of the instance
     */

    public void ACTS(View view) {
        TextView ae = findViewById(R.id.defecto);
        TextView ac = findViewById(R.id.elegir);
        if (ae.getVisibility() == View.VISIBLE) { //si es Visible lo pones Gone
            ae.setVisibility(View.GONE);
            ac.setVisibility(View.GONE);
        } else { // si no es Visible, lo pones
            ae.setVisibility(View.VISIBLE);
            ac.setVisibility(View.VISIBLE);
        }
    }
}
