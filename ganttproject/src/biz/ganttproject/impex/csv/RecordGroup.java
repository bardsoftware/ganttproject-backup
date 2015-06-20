/*
Copyright 2014 BarD Software s.r.o

This file is part of GanttProject, an opensource project management tool.

GanttProject is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

GanttProject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
*/
package biz.ganttproject.impex.csv;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import net.sourceforge.ganttproject.GPLogger;
import net.sourceforge.ganttproject.util.collect.Pair;

import org.apache.commons.csv.CSVRecord;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * Record group is a set of homogeneous CSV records. CSV file consists of a few
 * record groups separated with blank records. Each group may have its own header and
 * may have mandatory and optional fields.
 *
 * @author dbarashev (Dmitry Barashev)
 */
public abstract class RecordGroup {
  private final Set<String> myFields;
  private final Set<String> myMandatoryFields;
  private SetView<String> myCustomFields;
  private List<String> myHeader;
  private final String myName;
  private List<Pair<Level, String>> myErrorOutput;

  public RecordGroup(String name, Set<String> fields) {
    myName = name;
    myFields = fields;
    myMandatoryFields = fields;
  }

  public RecordGroup(String name, Set<String> regularFields, Set<String> mandatoryFields) {
    myName = name;
    myFields = regularFields;
    myMandatoryFields = mandatoryFields;
  }

  boolean isHeader(CSVRecord record) {
    Set<String> thoseFields = Sets.newHashSet();
    for (Iterator<String> it = record.iterator(); it.hasNext();) {
      thoseFields.add(it.next());
    }
    return thoseFields.containsAll(myMandatoryFields);
  }

  boolean process(CSVRecord record) {
    assert record.size() > 0;
    boolean allEmpty = true;
    for (Iterator<String> it = record.iterator(); it.hasNext();) {
      if (!Strings.isNullOrEmpty(it.next())) {
        allEmpty = false;
        break;
      }
    }
    if (allEmpty) {
      return false;
    }
    try {
      return doProcess(record);
    } catch (Throwable e) {
      GPLogger.getLogger(GanttCSVOpen.class).log(Level.WARNING, String.format("Failed to process record:\n%s", record), e);
      return false;
    }
  }

  protected boolean hasMandatoryFields(CSVRecord record) {
    for (String s : myMandatoryFields) {
      if (!record.isSet(s)) {
        return false;
      }
      if (Strings.isNullOrEmpty(record.get(s))) {
        return false;
      }
    }
    return true;
  }

  protected String getOrNull(CSVRecord record, String columnName) {
    if (!record.isMapped(columnName)) {
      return null;
    }
    return record.get(columnName);
  }

  protected boolean doProcess(CSVRecord record) {
    return (myHeader != null);
  }

  protected void postProcess() {}

  public void setHeader(List<String> header) {
    myHeader = header;
    myCustomFields = Sets.difference(Sets.newHashSet(header), myFields);
  }

  public List<String> getHeader() {
    return myHeader;
  }

  protected Collection<String> getCustomFields() {
    return myCustomFields;
  }

  @Override
  public String toString() {
    return myName;
  }

  void setErrorOutput(List<Pair<Level, String>> errors) {
    myErrorOutput = errors;
  }

  protected void addError(Level level, String message) {
    myErrorOutput.add(Pair.create(level, message));
  }
}