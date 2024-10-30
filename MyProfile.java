import java.nio.file.Path;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.Profile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.config.Arguments;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.ArrayList;
import java.util.List;

public class MyProfile implements Profile {
    public static void main(String[] args) {
        var arguments = Arguments.fromArgs(args)
            .withDefault("download", true)
            .withDefault("maxzoom", 6);
        String area = "monaco";
        Planetiler.create(arguments)
            .addOsmSource("osm", Path.of("data", area + ".osm.pbf"), "geofabrik:" + area)
            .overwriteOutput(Path.of("output.pmtiles"))
            .setProfile(new MyProfile())
            .run();
    }


    @Override
    public void processFeature(SourceFeature sourceFeature, FeatureCollector features) {
        if (sourceFeature.canBeLine()) {
            if (sourceFeature.hasTag("highway", "primary")) {
                features.line("lines")
                    .setPixelTolerance(0.0)
                    .setMinPixelSize(0.0);
            }
        }
    }

    @Override
    public List<VectorTile.Feature> postProcessLayerFeatures(String layer, int zoom,
            List<VectorTile.Feature> items) throws GeometryException {
        List<VectorTile.Feature> result = new ArrayList<>();
        
        double minLength = 4 * 0.0625;
        double tolerance = -1;
        double buffer = 4.0;
        double loopMinLength = 4 * 0.0625;

        result = MyFeatureMerge.mergeLineStrings(items, minLength, tolerance, buffer, loopMinLength);
        
        return result;
    }

    @Override
    public String attribution() {
        return OSM_ATTRIBUTION;
    }

    @Override
    public boolean isOverlay() {
        return true;
    }
}