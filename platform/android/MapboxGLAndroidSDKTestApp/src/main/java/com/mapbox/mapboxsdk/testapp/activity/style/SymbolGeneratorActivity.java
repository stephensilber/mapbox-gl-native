package com.mapbox.mapboxsdk.testapp.activity.style;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.style.utils.SymbolGenerator;
import com.mapbox.mapboxsdk.testapp.R;
import com.mapbox.mapboxsdk.testapp.utils.ResourceUtils;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.Geometry;
import com.mapbox.services.commons.geojson.custom.GeometryDeserializer;
import com.mapbox.services.commons.geojson.custom.PositionDeserializer;
import com.mapbox.services.commons.models.Position;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.Filter.eq;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class SymbolGeneratorActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final String GENERATED_SYMBOL_SOURCE_ID = "com.mapbox.mapboxsdk.style.layers.symbol.source.id";
  private static final String GENERATED_SYMBOL_LAYER_ID_BASE = "com.mapbox.mapboxsdk.style.layers.symbol.layer.id.";
  private static final String GENERATED_SYMBOL_IMAGE_ID_BASE = "com.mapbox.mapboxsdk.style.layers.symbol.image.id.";
  private static final String KEY_FEATURE_ID = "brk_name";

  private final List<String> layerIds = new ArrayList<>();

  private MapView mapView;
  private MapboxMap mapboxMap;

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

    try {
      // read local geojson from raw folder
      String tinyCountriesJson = ResourceUtils.readRawResource(this, R.raw.tiny_countries);

      // convert geojson to a model
      FeatureCollection featureCollection = new GsonBuilder()
        .registerTypeAdapter(Geometry.class, new GeometryDeserializer())
        .registerTypeAdapter(Position.class, new PositionDeserializer())
        .create().fromJson(tinyCountriesJson, FeatureCollection.class);

      // add a geojson to the map
      Source source = new GeoJsonSource(GENERATED_SYMBOL_SOURCE_ID, featureCollection);
      mapboxMap.addSource(source);

      // for each feature add a symbolLayer
      for (Feature feature : featureCollection.getFeatures()) {
        String countryName = feature.getStringProperty(KEY_FEATURE_ID);

        // create View
        TextView textView = new TextView(this);
        textView.setBackgroundColor(getResources().getColor(R.color.blueAccent));
        textView.setPadding(10, 5 , 10 , 5);
        textView.setTextColor(Color.WHITE);
        textView.setText(countryName);

        // create bitmap from view
        String iconId = GENERATED_SYMBOL_IMAGE_ID_BASE + countryName;
        mapboxMap.addImage(iconId, SymbolGenerator.generate(textView));

        // create layer for bitmap and filter source on name
        String layerId = GENERATED_SYMBOL_LAYER_ID_BASE + countryName;
        layerIds.add(layerId);
        mapboxMap.addLayer(new SymbolLayer(layerId, GENERATED_SYMBOL_SOURCE_ID)
          .withProperties(
            iconImage(iconId),
            iconAllowOverlap(false)
          ).withFilter(
            eq(KEY_FEATURE_ID, countryName)
          )
        );
      }

      addSymbolClickListener();
    } catch (IOException exception) {
      Timber.e(exception);
    }
  }

  private void addSymbolClickListener() {
    mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
      @Override
      public void onMapClick(@NonNull LatLng point) {
        PointF screenPoint = mapboxMap.getProjection().toScreenLocation(point);
        String[] layerIdsArray = layerIds.toArray(new String[layerIds.size()]);
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, layerIdsArray);
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
      for (String layerId : layerIds) {
        SymbolLayer layer = mapboxMap.getLayerAs(layerId);
        layer.setProperties(iconAllowOverlap(layer.getIconAllowOverlap().getValue() ? false : true));
      }
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
