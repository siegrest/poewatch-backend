package watch.poe.app.domain.statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatType {
  TIME_API_REPLY_DOWNLOAD(StatGroupType.AVG, TimeFrame.M_60),
  TIME_API_TTFB(StatGroupType.AVG, TimeFrame.M_60),

  TIME_REPLY_PARSE(StatGroupType.AVG, TimeFrame.M_60),
  TIME_REPLY_DESERIALIZE(StatGroupType.AVG, TimeFrame.M_60),
  TIME_INDEX_ITEM(StatGroupType.AVG, TimeFrame.M_60),
  //  TIME_PERSIST_ENTRY(StatGroupType.AVG, TimeFrame.M_60),
  TIME_PROCESS_RIVER(StatGroupType.AVG, TimeFrame.M_60),
  TIME_PERSIST_STASHES(StatGroupType.AVG, TimeFrame.M_60),
  TIME_MARK_STASHES_STALE(StatGroupType.AVG, TimeFrame.M_60),
  TIME_PERSIST_STASH_ENTRIES(StatGroupType.AVG, TimeFrame.M_60),
  TIME_PERSIST_ACCOUNT(StatGroupType.AVG, TimeFrame.M_60),
  TIME_PERSIST_CHARACTER(StatGroupType.AVG, TimeFrame.M_60),

  COUNT_API_ERRORS_READ_TIMEOUT(StatGroupType.COUNT, TimeFrame.M_60),
  COUNT_API_ERRORS_CONNECT_TIMEOUT(StatGroupType.COUNT, TimeFrame.M_60),
  COUNT_API_ERRORS_CONN_RESET(StatGroupType.COUNT, TimeFrame.M_60),
  COUNT_API_ERRORS_5XX(StatGroupType.COUNT, TimeFrame.M_60),
  COUNT_API_ERRORS_4XX(StatGroupType.COUNT, TimeFrame.M_60),
  COUNT_API_ERRORS_DUPLICATE(StatGroupType.SUM, TimeFrame.M_60),

  COUNT_REPLY_SIZE(StatGroupType.AVG, TimeFrame.M_60),
  COUNT_API_CALLS(StatGroupType.COUNT, TimeFrame.M_60),
  COUNT_TOTAL_STASHES(StatGroupType.SUM, TimeFrame.M_60),
  COUNT_TOTAL_ITEMS(StatGroupType.SUM, TimeFrame.M_60),
  //  COUNT_ITEMS_DISCARDED_INVALID_LEAGUE(StatGroupType.SUM, TimeFrame.M_60),
  COUNT_ACCEPTED_ITEMS(StatGroupType.SUM, TimeFrame.M_60),
  COUNT_ACTIVE_ACCOUNTS(StatGroupType.SUM, TimeFrame.M_60),

  // todo: discarded total, accepted total

  MISC_APP_STARTUP(StatGroupType.COUNT, TimeFrame.M_60),
  MISC_APP_SHUTDOWN(StatGroupType.COUNT, TimeFrame.M_60);

  private StatGroupType statGroupType;
  private TimeFrame timeFrame;
}
