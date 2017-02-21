package com.adammusciano.mapboxmap;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.MalformedURLException;
import java.net.URL;

import static com.mapbox.mapboxsdk.style.layers.Filter.all;
import static com.mapbox.mapboxsdk.style.layers.Filter.gte;
import static com.mapbox.mapboxsdk.style.layers.Filter.lt;
import static com.mapbox.mapboxsdk.style.layers.Filter.neq;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleBlur;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, getString(R.string.access_token));

        setContentView(R.layout.activity_main);
        mapView= (MapView)findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);


        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                addClusteredGeoJsonSource(mapboxMap);

                mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {
                        Toast.makeText(MainActivity.this, point.toString() + "\nZoom: " + mapboxMap.getCameraPosition().zoom, Toast.LENGTH_SHORT).show();

                    }
                });
                // Interact with the map using mapboxMap here

            }
        });
    }

    private void addClusteredGeoJsonSource(MapboxMap mapboxMap) {

        // Add a new source from our GeoJSON data and set the 'cluster' option to true.
        try {
            mapboxMap.addSource(
                    // Point to GeoJSON data. This example visualizes all M1.0+ earthquakes from
                    // 12/22/15 to 1/21/16 as logged by USGS' Earthquake hazards program.
                    new GeoJsonSource("earthquakes",
                            new URL("https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson"),
                            new GeoJsonOptions()
                                    .withCluster(false)
                                    .withClusterMaxZoom(15) // Max zoom to cluster points on
                                    .withClusterRadius(20) // Use small cluster radius for the heatmap look
                    )
            );
        } catch (MalformedURLException malformedUrlException) {
            Log.e("heatmapActivity", "Check the URL " + malformedUrlException.getMessage());
        }

        // Use the earthquakes source to create four layers:
        // three for each cluster category, and one for unclustered points

        // Each point range gets a different fill color.
        final int[][] layers = new int[][]{
                new int[]{15, Color.parseColor("#E55E5E")},
                new int[]{5, Color.parseColor("#F9886C")},
                new int[]{0, Color.parseColor("#FBB03B")}
        };

        CircleLayer unclustered = new CircleLayer("unclustered-points", "earthquakes");
        unclustered.setProperties(
                circleColor(Color.parseColor("#FBB03B")),
                circleRadius(10f),
                circleBlur(5f));
        unclustered.setFilter(
                neq("cluster", true)
        );
        mapboxMap.addLayer(unclustered, "building");

        for (int i = 0; i < layers.length; i++) {

            CircleLayer circles = new CircleLayer("cluster-" + i, "earthquakes");

//            Log.println(Log.DEBUG,"ZOOM LEVEL",""+mapboxMap.getCameraPosition().zoom);

            circles.setProperties(
                    circleColor(layers[i][1]),
                    circleRadius(70f),
                    circleBlur(1f)
            );
            circles.setFilter(
                    i == 0
                            ? gte("point_count", layers[i][0]) :
                            all(gte("point_count", layers[i][0]), lt("point_count", layers[i - 1][0]))
            );
            mapboxMap.addLayer(circles, "building");
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
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
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
