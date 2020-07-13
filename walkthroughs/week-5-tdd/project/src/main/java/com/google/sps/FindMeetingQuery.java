// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {

  /**
   * Queries a collection of available time ranges that could accommodate the meeting request
   * @param {Collection<Event>} events The collection of events that may conflict with the reqest
   * @param {MeetingRequest} request The request to query abailable time ranges of
   * @return {Collection<TimeRange>} The time ranges that could accommodate the meeting request
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Initializes and gets colllections that will be used locally to query
    List<TimeRange> blockedTimes = new ArrayList<TimeRange>();
    Collection<TimeRange> result = new ArrayList<TimeRange>();
    Collection<String> requestAttendees = request.getAttendees();

    // Finds the set of events that block time ranges for being a valid meeting time
    for (Event event : events) {
      Set<String> eventAttendees = event.getAttendees();
      boolean isBlocked = false;
      for (String attendee : eventAttendees) {
        if (requestAttendees.contains(attendee)) {
          isBlocked = true;
          break;
        }
      }
      if (isBlocked) {
        blockedTimes.add(event.getWhen());
      }
    }

    // Sorts the blocked time ranges by start time
    blockedTimes.sort((timeRange1, timeRange2) -> {
      if (timeRange1.start() < timeRange2.start()) {
        return -1;
      } else {
        return 1;
      }
    });

    // Gets the next available time range by saving the end time of the last blocked time
    int lastEndTime = TimeRange.START_OF_DAY;
    for (int i = 0; i < blockedTimes.size(); i++) {
      int eventStart = blockedTimes.get(i).start();
      int eventEnd = blockedTimes.get(i).end();
      if (eventStart > lastEndTime && (int) request.getDuration() <= eventStart - lastEndTime) {
        result.add(TimeRange.fromStartEnd(lastEndTime, eventStart, false));
      }
      lastEndTime = (lastEndTime > eventEnd) ? lastEndTime : eventEnd;
    }

    // Takes into account the final possible availalbe 
    if (lastEndTime < TimeRange.END_OF_DAY && (int) request.getDuration() <= (TimeRange.END_OF_DAY - lastEndTime)) {
      result.add(TimeRange.fromStartEnd(lastEndTime, TimeRange.END_OF_DAY, true));
    }

    return result;
  }
}
