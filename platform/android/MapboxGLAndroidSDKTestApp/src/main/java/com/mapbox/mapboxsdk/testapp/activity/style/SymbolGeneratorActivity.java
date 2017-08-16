package com.mapbox.mapboxsdk.testapp.activity.style;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.style.utils.SymbolGenerator;
import com.mapbox.mapboxsdk.testapp.R;
import com.mapbox.services.commons.geojson.Feature;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class SymbolGeneratorActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final String GENERATED_SYMBOL_LAYER_ID = "com.mapbox.mapboxsdk.testapp.style.layers.symbol.layer.id";
  private static final String GENERATED_SYMBOL_SOURCE_ID = "com.mapbox.mapboxsdk.testapp.style.layers.symbol.source.id";
  private static final String GENERATED_SYMBOL_IMAGE_ID = "com.mapbox.mapboxsdk.testapp.style.layers.symbol.layer.id";
  private static final String GENERATED_SYMBOL_SOURCE_URL = "https://d2ad6b4ur7yvpq.cloudfront.net/naturalearth-3.3.0/"
    + "ne_110m_admin_0_tiny_countries.geojson";

  private MapView mapView;
  private MapboxMap mapboxMap;
  private SymbolLayer layer;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_symbol_generator);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap map) {
    mapboxMap = map;

    Button button = new Button(this);
    button.setText("Hello world");

    Bitmap bitmap = SymbolGenerator.generate(button);
    mapboxMap.addImage(GENERATED_SYMBOL_IMAGE_ID, bitmap);

    setupSymbolLayer();
    addSymbolClickListener();
  }

  private void setupSymbolLayer() {
    try {
      // add source for symbol
      Source source = new GeoJsonSource(GENERATED_SYMBOL_SOURCE_ID,
        new URL(GENERATED_SYMBOL_SOURCE_URL)
      );
      mapboxMap.addSource(source);

      // add layer for symbol
      layer = new SymbolLayer(GENERATED_SYMBOL_LAYER_ID, GENERATED_SYMBOL_SOURCE_ID)
        .withProperties(
          iconImage(GENERATED_SYMBOL_IMAGE_ID),
          iconAllowOverlap(false)
        );
      mapboxMap.addLayer(layer);
    } catch (MalformedURLException exception) {
      Timber.e(exception);
    }
  }

  private void addSymbolClickListener() {
    mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
      @Override
      public void onMapClick(@NonNull LatLng point) {
        PointF screenPoint = mapboxMap.getProjection().toScreenLocation(point);
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, GENERATED_SYMBOL_LAYER_ID);
        if (!features.isEmpty()) {
          Timber.v("Feature was clicked with data: %s", features.get(0).toJson());
          Toast.makeText(
            SymbolGeneratorActivity.this,
            "hello from: " + features.get(0).getStringProperty("name_sort"),
            Toast.LENGTH_LONG).show();
        }
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_generator_symbol, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_action_icon_overlap) {
      layer.setProperties(iconAllowOverlap(layer.getIconAllowOverlap().getValue() ? false : true));
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }
}
