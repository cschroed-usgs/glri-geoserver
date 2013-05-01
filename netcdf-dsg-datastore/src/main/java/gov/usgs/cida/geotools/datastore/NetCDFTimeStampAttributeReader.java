package gov.usgs.cida.geotools.datastore;

import java.io.IOException;
import java.net.URL;
import java.util.Formatter;
import java.util.NoSuchElementException;
import org.geotools.data.AttributeReader;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.StationTimeSeriesFeature;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;

/**
 *
 * @author tkunicki
 */
public class NetCDFTimeStampAttributeReader implements AttributeReader {
    
    private final FeatureDataset featureDataset;
    private final SimpleFeatureType featureType;
    private final StationTimeSeriesFeatureCollection stationTimeSeriesFeatureCollection;
    private final StationTimeSeriesFeature stationTimeSeriesFeature;
    private PointFeature pointFeature;
    
    
    NetCDFTimeStampAttributeReader(URL netCDFURL, SimpleFeatureType featureType) throws IOException {
        this.featureType = featureType;
        this.featureDataset = NetCDFUtil.acquireDataSet(netCDFURL);
        
        stationTimeSeriesFeatureCollection = NetCDFUtil.extractStationTimeSeriesFeatureCollection(featureDataset);
        stationTimeSeriesFeatureCollection.resetIteration();
        stationTimeSeriesFeature = (stationTimeSeriesFeatureCollection != null && stationTimeSeriesFeatureCollection.hasNext()) ?
                stationTimeSeriesFeatureCollection.next() :
                null;
        if (stationTimeSeriesFeature == null) {
            throw new IllegalStateException("NetCDF FeatureDataset doesn't contain StationTimeSeriesFeatureCollection");
        }
    }

    @Override
    public int getAttributeCount() {
        return featureType.getAttributeCount();
    }

    @Override
    public AttributeDescriptor getAttributeType(int index) throws ArrayIndexOutOfBoundsException {
        return featureType.getDescriptor(index);
    }

    @Override
    public void close() throws IOException {
        stationTimeSeriesFeature.finish();
        stationTimeSeriesFeatureCollection.finish();
        try {
            featureDataset.close();
        } catch (IOException e) {
            // don't care
        }
    }

    @Override
    public boolean hasNext() throws IOException {
        return stationTimeSeriesFeature.hasNext();
    }

    @Override
    public void next() throws IOException, IllegalArgumentException, NoSuchElementException {
        pointFeature = stationTimeSeriesFeature.next();
        if (pointFeature == null) {
            throw new NoSuchElementException();
        }
    }

    @Override
    public Object read(int index) throws IOException, ArrayIndexOutOfBoundsException {
        if (index < 1) {
            return pointFeature.getObservationTimeAsCalendarDate().toDate();
        } else {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }
    
}
