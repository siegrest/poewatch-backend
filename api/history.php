<?php
function error($code, $msg) {
  http_response_code($code);
  die( json_encode( array("error" => $msg) ) );
}

function get_league_data($pdo, $id) {
  $query = "SELECT 
    l.id AS id_l, l.name AS league, 
    i.mean, i.median, i.mode, i.exalted, i.count, i.quantity,
    GROUP_CONCAT(h.mean      ORDER BY h.time DESC) AS mean_list,
    GROUP_CONCAT(h.median    ORDER BY h.time DESC) AS median_list,
    GROUP_CONCAT(h.mode      ORDER BY h.time DESC) AS mode_list,
    GROUP_CONCAT(h.quantity  ORDER BY h.time DESC) AS quantity_list,
    GROUP_CONCAT(h.inc       ORDER BY h.time DESC) AS inc_list,
    GROUP_CONCAT(h.time      ORDER BY h.time DESC) AS time_list
  FROM      league_history_daily_rolling AS h
  JOIN      data_leagues    AS l  ON h.id_l  = l.id
  JOIN      league_items    AS i  ON i.id_l  = l.id AND i.id_d = h.id_d
  WHERE     h.id_d = ?
  GROUP BY  h.id_l, h.id_d";

  $stmt = $pdo->prepare($query);
  $stmt->execute([$id]);

  return $stmt;
}

function get_item_data($pdo, $id) {
  $query = "SELECT 
    d.name, d.type, d.frame, d.icon,
    d.tier, d.lvl, d.quality, d.corrupted, 
    d.links, d.var AS variation,
    cp.name AS categoryParent, cc.name AS categoryChild
  FROM      data_itemData   AS d
  JOIN      category_parent AS cp ON d.id_cp = cp.id 
  LEFT JOIN category_child  AS cc ON d.id_cc = cc.id 
  WHERE     d.id = ?
  LIMIT     1";

  $stmt = $pdo->prepare($query);
  $stmt->execute([$id]);

  return $stmt;
}

function parse_history_data($stmt) {
  $payload = array();

  while ($row = $stmt->fetch()) {
    // Convert CSVs to arrays
    $means    = explode(',', $row['mean_list']);
    $medians  = explode(',', $row['median_list']);
    $modes    = explode(',', $row['mode_list']);
    $quants   = explode(',', $row['quantity_list']);
    $incs     = explode(',', $row['inc_list']);
    $times    = explode(',', $row['time_list']);

    // Form a temporary entry array
    $tmp = array(
      'leagueId'      => (int)    $row['id_l'],
      'league'        =>          $row['league'],
      'mean'          => (float)  $row['mean'],
      'median'        => (float)  $row['median'],
      'mode'          => (float)  $row['mode'],
      'exalted'       => (float)  $row['exalted'],
      'count'         => (int)    $row['count'],
      'quantity'      => (int)    $row['quantity'],
      'history'       =>          array()
    );

    for ($i = 0; $i < sizeof($means); $i++) { 
      $tmp['history'][ $times[$i] ] = array(
        'mean'     => (float) $means[$i],
        'median'   => (float) $medians[$i],
        'mode'     => (float) $modes[$i],
        'quantity' => (int)   $quants[$i],
        'inc'      => (int)   $incs[$i]
      );
    }

    $payload[] = $tmp;
  }

  return $payload;
}

function form_payload($itemData, $historyData) {
  $payload = $itemData;

  if ($payload['tier']      !== null) $payload['tier']      = (int)   $payload['tier'];
  if ($payload['lvl']       !== null) $payload['lvl']       = (int)   $payload['lvl'];
  if ($payload['quality']   !== null) $payload['quality']   = (int)   $payload['quality'];
  if ($payload['corrupted'] !== null) $payload['corrupted'] = (bool)  $payload['corrupted'];
  if ($payload['links']     !== null) $payload['links']     = (int)   $payload['links'];

  $payload["leagues"] = $historyData;
  return $payload;
}

// Define content type
header("Content-Type: application/json");

// Get parameters
if (!isset($_GET["id"])) error(400, "Missing id parameter");

// Connect to database
include_once ( "details/pdo.php" );

// Get item's name, frame, icon, etc.
$stmt = get_item_data($pdo, $_GET["id"]);
// If no results with provided id
if ($stmt->rowCount() === 0) error(400, "Invalid id parameter");
// Get the one row of item data
$itemData = $stmt->fetch();

// Get league-specific data from database
$stmt = get_league_data($pdo, $_GET["id"]);
// Parse received league-specific data
$historyData = parse_history_data($stmt);

// Form the payload
$payload = form_payload($itemData, $historyData);

// Display generated data
echo json_encode($payload, JSON_PRESERVE_ZERO_FRACTION);
