import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class SaffSqueeze {

    @Test
    public void one_full_test() {
        String expectedOutput = "Welcome to the Ferry Finding System\n" + "=======\n" + "Ferry Time Table\n" + "\n"
                + "Departures from Port Ellen\n" + "\n"
                + " --------------------------------------------------------------------------\n"
                + "| Time     | Destination   | Journey Time  | Ferry              | Arrives  |\n"
                + " --------------------------------------------------------------------------\n"
                + "| 00:00    | Mos Eisley    | 00:30         | Titanic            | 00:30    |\n"
                + "| 00:10    | Tarsonis      | 00:45         | Hyperion           | 00:55    |\n"
                + "| 00:20    | Mos Eisley    | 00:30         | Millenium Falcon   | 00:50    |\n"
                + "| 00:40    | Mos Eisley    | 00:30         | Golden Hind        | 01:10    |\n"
                + "| 01:00    | Mos Eisley    | 00:30         | Enterprise         | 01:30    |\n"
                + "| 01:10    | Tarsonis      | 00:45         | Hood               | 01:55    |\n"
                + "| 01:20    | Mos Eisley    | 00:30         | Tempest            | 01:50    |\n"
                + "| 01:40    | Mos Eisley    | 00:30         | Dreadnaught        | 02:10    |\n" + "\n"
                + "Departures from Mos Eisley\n" + "\n"
                + " --------------------------------------------------------------------------\n"
                + "| Time     | Destination   | Journey Time  | Ferry              | Arrives  |\n"
                + " --------------------------------------------------------------------------\n"
                + "| 00:10    | Port Ellen    | 00:30         | Enterprise         | 00:40    |\n"
                + "| 00:30    | Port Ellen    | 00:30         | Tempest            | 01:00    |\n"
                + "| 00:40    | Tarsonis      | 00:35         | Black Pearl        | 01:15    |\n"
                + "| 00:50    | Port Ellen    | 00:30         | Titanic            | 01:20    |\n"
                + "| 01:10    | Port Ellen    | 00:30         | Millenium Falcon   | 01:40    |\n"
                + "| 01:30    | Port Ellen    | 00:30         | Golden Hind        | 02:00    |\n"
                + "| 01:40    | Tarsonis      | 00:35         | Defiant            | 02:15    |\n"
                + "| 01:50    | Port Ellen    | 00:30         | Enterprise         | 02:20    |\n" + "\n"
                + "Departures from Tarsonis\n" + "\n"
                + " --------------------------------------------------------------------------\n"
                + "| Time     | Destination   | Journey Time  | Ferry              | Arrives  |\n"
                + " --------------------------------------------------------------------------\n"
                + "| 00:25    | Port Ellen    | 00:45         | Dreadnaught        | 01:10    |\n"
                + "| 00:40    | Mos Eisley    | 00:35         | Defiant            | 01:15    |\n"
                + "| 01:25    | Port Ellen    | 00:45         | Hyperion           | 02:10    |\n"
                + "| 01:40    | Mos Eisley    | 00:35         | Black Pearl        | 02:15    |\n" + "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        Program.start(ps);

        assertEquals(expectedOutput, baos.toString());
    }

    @Test
    public void two_inlineProgram_check_ferry_name() {
        Program.wireUp();

        List<Port> allPorts = Program.ports.all();
        List<TimeTableViewModelRow> timeTable = Program.timeTableService.getTimeTable(allPorts);
        Port port = allPorts.iterator().next();
        List<TimeTableViewModelRow> items = new ArrayList<TimeTableViewModelRow>();
        for (TimeTableViewModelRow x : timeTable) {
            if (x.originPort.equals(port.name)) {
                items.add(x);
            }
        }
        Collections.sort(items, new Comparator<TimeTableViewModelRow>() {

            @Override
            public int compare(TimeTableViewModelRow tt1, TimeTableViewModelRow tt2) {
                return tt1.startTime.compareTo(tt2.startTime);
            }
        });

        TimeTableViewModelRow item = items.get(0);
        assertThat(item.ferryName, is("Titanic"));
    }

    @Test
    public void three_inline_getTimetable_check_ferry_is_not_null() {
        TimeTables timeTables = new TimeTables();
        Ferries ferries = new Ferries();
        Ports ports = new Ports();
        FerryAvailabilityService ferryService = new FerryAvailabilityService(timeTables,
                new PortManager(ports, ferries));

        List<Port> allPorts = ports.all();
        List<TimeTable> timetables = timeTables.all();
        List<TimeTableEntry> allEntries = new ArrayList<TimeTableEntry>();
        for (TimeTable tt : timetables) {
            allEntries.addAll(tt.entries);
        }
        Collections.sort(allEntries, new Comparator<TimeTableEntry>() {

            @Override
            public int compare(TimeTableEntry tte1, TimeTableEntry tte2) {
                return Long.compare(tte1.time, tte2.time);
            }
        });

        TimeTableEntry timetable = allEntries.get(0);
        Port origin = null;
        Port destination = null;
        for (Port x1 : allPorts) {
            if (x1.id == timetable.originId) {
                origin = x1;
            }
            if (x1.id == timetable.destinationId) {
                destination = x1;
            }
        }
        String destinationName = destination.name;
        Ferry ferry = ferryService.nextFerryAvailableFrom(origin.id, timetable.time);
        TimeTableViewModelRow row = new TimeTableViewModelRow();
        row.destinationPort = destinationName;

        assertThat(ferry, is(notNullValue()));
    }

    @Test
    public void four_inline_nextFerryAvailableFrom() {
        TimeTables timeTables = new TimeTables();
        Ferries ferries = new Ferries();
        Ports ports = new Ports();
        PortManager portManager = new PortManager(ports, ferries);

        List<TimeTable> timetables = timeTables.all();
        List<TimeTableEntry> allEntries = new ArrayList<TimeTableEntry>();
        for (TimeTable tt : timetables) {
            allEntries.addAll(tt.entries);
        }
        Collections.sort(allEntries, new Comparator<TimeTableEntry>() {

            @Override
            public int compare(TimeTableEntry tte1, TimeTableEntry tte2) {
                return Long.compare(tte1.time, tte2.time);
            }
        });

        List<PortModel> ports1 = portManager.portModels();
        List<TimeTableEntry> allEntries1 = new ArrayList<TimeTableEntry>();
        for (TimeTable tt1 : timeTables.all()) {
            allEntries1.addAll(tt1.entries);
        }
        Collections.sort(allEntries1, new Comparator<TimeTableEntry>() {

            @Override
            public int compare(TimeTableEntry tte1, TimeTableEntry tte2) {
                return Long.compare(tte1.time, tte2.time);
            }
        });

        TimeTableEntry entry = allEntries1.iterator().next();
        FerryJourney ferry1 = FerryManager.createFerryJourney(ports1, entry);
        if (ferry1 != null) {
            if (ferry1.ferry == null) {
                ferry1.ferry = ferry1.origin.getNextAvailable(entry.time);

                assertThat(ferry1.ferry, is(notNullValue()));
            }
        }
    }

    @Test
    public void five_inline_getNextAvailable() {
        TimeTables timeTables = new TimeTables();
        Ferries ferries = new Ferries();
        Ports ports = new Ports();

        List<TimeTableEntry> allEntries = new ArrayList<>();
        for (TimeTable tt : timeTables.all()) {
            allEntries.addAll(tt.entries);
        }
        Collections.sort(allEntries, new Comparator<TimeTableEntry>() {

            @Override
            public int compare(TimeTableEntry tte1, TimeTableEntry tte2) {
                return Long.compare(tte1.time, tte2.time);
            }
        });
        List<PortModel> allPorts = new ArrayList<>();
        for (Port port2 : ports.all()) {
            allPorts.add(new PortModel(port2));
        }
        for (Ferry ferry : ferries.all()) {
            for (PortModel port1 : allPorts) {
                if (port1.id == ferry.homePortId) {
                    port1.addBoat(0, ferry);
                }
            }
        }

        List<TimeTableEntry> allEntries1 = new ArrayList<>();
        for (TimeTable tt1 : timeTables.all()) {
            allEntries1.addAll(tt1.entries);
        }
        Collections.sort(allEntries1, new Comparator<TimeTableEntry>() {

            @Override
            public int compare(TimeTableEntry tte1, TimeTableEntry tte2) {
                return Long.compare(tte1.time, tte2.time);
            }
        });

        TimeTableEntry entry = allEntries1.get(0);
        FerryJourney fj = new FerryJourney();
        for (PortModel port : allPorts) {
            if (port.id == entry.originId) {
                fj.origin = port;
            }
            if (port.id == entry.destinationId) {
                fj.destination = port;
            }
        }
        Map.Entry<Integer, Long> entry1 = fj.origin.boatAvailability.entrySet().iterator().next();
        assertTrue(entry.time >= entry1.getValue());
    }
}
