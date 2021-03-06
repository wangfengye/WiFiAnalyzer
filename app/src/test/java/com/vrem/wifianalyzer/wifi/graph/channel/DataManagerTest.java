/*
 * WiFi Analyzer
 * Copyright (C) 2017  VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.vrem.wifianalyzer.wifi.graph.channel;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.TitleLineGraphSeries;
import com.vrem.wifianalyzer.BuildConfig;
import com.vrem.wifianalyzer.RobolectricUtil;
import com.vrem.wifianalyzer.wifi.band.WiFiBand;
import com.vrem.wifianalyzer.wifi.band.WiFiChannel;
import com.vrem.wifianalyzer.wifi.band.WiFiWidth;
import com.vrem.wifianalyzer.wifi.graph.tools.DataPointsEquals;
import com.vrem.wifianalyzer.wifi.graph.tools.GraphViewWrapper;
import com.vrem.wifianalyzer.wifi.model.WiFiAdditional;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.model.WiFiSignal;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class DataManagerTest {
    private static final int LEVEL = -40;

    private DataManager fixture;

    @Before
    public void setUp() {
        RobolectricUtil.INSTANCE.getActivity();
        fixture = new DataManager();
    }

    @Test
    public void testFrequencyAdjustment() throws Exception {
        assertEquals(10, DataManager.frequencyAdjustment(10));
        assertEquals(10, DataManager.frequencyAdjustment(11));
        assertEquals(10, DataManager.frequencyAdjustment(12));
        assertEquals(10, DataManager.frequencyAdjustment(13));
        assertEquals(10, DataManager.frequencyAdjustment(14));
        assertEquals(15, DataManager.frequencyAdjustment(15));
    }

    @Test
    public void testGetNewSeries() throws Exception {
        // setup
        Pair<WiFiChannel, WiFiChannel> wiFiChannelPair = WiFiBand.GHZ2.getWiFiChannels().getWiFiChannelPairs().get(0);
        List<WiFiDetail> expected = makeWiFiDetails(wiFiChannelPair.first.getFrequency());
        // execute
        Set<WiFiDetail> actual = fixture.getNewSeries(expected, wiFiChannelPair);
        // validate
        assertEquals(expected.size() - 1, actual.size());
        assertTrue(actual.contains(expected.get(0)));
        assertFalse(actual.contains(expected.get(1)));
        assertTrue(actual.contains(expected.get(2)));
    }

    @Test
    public void testGetDataPoints() throws Exception {
        // setup
        WiFiDetail expected = makeWiFiDetail("SSID", 2455);
        // execute
        DataPoint[] actual = fixture.getDataPoints(expected);
        // validate
        assertEquals(5, actual.length);
        assertEquals(new DataPoint(2445, -100).toString(), actual[0].toString());
        assertEquals(new DataPoint(2450, LEVEL).toString(), actual[1].toString());
        assertEquals(new DataPoint(2455, LEVEL).toString(), actual[2].toString());
        assertEquals(new DataPoint(2460, LEVEL).toString(), actual[3].toString());
        assertEquals(new DataPoint(2465, -100).toString(), actual[4].toString());
    }

    @Test
    public void testAddSeriesDataWithExistingWiFiDetails() throws Exception {
        // setup
        GraphViewWrapper graphViewWrapper = mock(GraphViewWrapper.class);
        WiFiDetail wiFiDetail = makeWiFiDetail("SSID", 2455);
        //noinspection ArraysAsListWithZeroOrOneArgument
        Set<WiFiDetail> wiFiDetails = new HashSet<>(Arrays.asList(wiFiDetail));
        DataPoint[] dataPoints = fixture.getDataPoints(wiFiDetail);
        when(graphViewWrapper.isNewSeries(wiFiDetail)).thenReturn(false);
        // execute
        fixture.addSeriesData(graphViewWrapper, wiFiDetails);
        // validate
        verify(graphViewWrapper).isNewSeries(wiFiDetail);
        verify(graphViewWrapper).updateSeries(
            argThat(equalTo(wiFiDetail)), argThat(new DataPointsEquals(dataPoints)), argThat(equalTo(Boolean.TRUE)));
    }

    @Test
    public void testAddSeriesDataNewWiFiDetails() throws Exception {
        // setup
        GraphViewWrapper graphViewWrapper = mock(GraphViewWrapper.class);
        WiFiDetail wiFiDetail = makeWiFiDetail("SSID", 2455);
        //noinspection ArraysAsListWithZeroOrOneArgument
        Set<WiFiDetail> wiFiDetails = new HashSet<>(Arrays.asList(wiFiDetail));
        when(graphViewWrapper.isNewSeries(wiFiDetail)).thenReturn(true);
        // execute
        fixture.addSeriesData(graphViewWrapper, wiFiDetails);
        // validate
        verify(graphViewWrapper).isNewSeries(wiFiDetail);
        //noinspection unchecked
        verify(graphViewWrapper).addSeries(
            argThat(equalTo(wiFiDetail)), any(TitleLineGraphSeries.class), argThat(equalTo(Boolean.TRUE)));
    }

    private WiFiDetail makeWiFiDetail(@NonNull String SSID, int frequency) {
        WiFiSignal wiFiSignal = new WiFiSignal(frequency, frequency, WiFiWidth.MHZ_20, LEVEL);
        return new WiFiDetail(SSID, "BSSID", StringUtils.EMPTY, wiFiSignal, WiFiAdditional.EMPTY);
    }

    private List<WiFiDetail> makeWiFiDetails(int frequency) {
        return Arrays.asList(makeWiFiDetail("SSID1", frequency), makeWiFiDetail("SSID2", -frequency), makeWiFiDetail("SSID3", frequency));
    }
}
