<?php
function getOldHistory($PARAM_category, $PARAM_index) {
  $payload = array();

  $baseDir = dirname( getcwd(), 2) . "/data/new_history/*";
  $leagueDirs = glob($baseDir, GLOB_ONLYDIR);

  foreach ( $leagueDirs as $leagueDir ) {
    $categoryFile = $leagueDir . "/$PARAM_category.json";

    if ( !file_exists($categoryFile) ) continue;

    $json = json_decode( file_get_contents( $categoryFile ) , true );
    
    if ( array_key_exists($PARAM_index, $json) ) {
      $splitDir = explode("/", $leagueDir);
      $league = end( $splitDir );
      $payload[$league] = $json[$PARAM_index];
    }
  }

  return $payload;
}

function getNewHistory($PARAM_category, $PARAM_index) {
  $payload = array();

  $baseDir = dirname( getcwd(), 2) . "/data/history/*";
  $leagueDirs = glob($baseDir, GLOB_ONLYDIR);

  foreach ( $leagueDirs as $leagueDir ) {
    $categoryFile = $leagueDir . "/$PARAM_category.json";

    if ( !file_exists($categoryFile) ) continue;

    $json = json_decode( file_get_contents( $categoryFile ) , true );
    
    if ( array_key_exists($PARAM_index, $json) ) {
      $splitDir = explode("/", $leagueDir);
      $league = end( $splitDir );
      $payload[$league] = $json[$PARAM_index]["mean"];
    }
  }

  return $payload;
}

function getPriceData($PARAM_category, $PARAM_index) {
  $payload = array();

  $baseDir = dirname( getcwd(), 2) . "/data/output/*";
  $leagueDirs = glob($baseDir, GLOB_ONLYDIR);

  foreach ( $leagueDirs as $leagueDir ) {
    $categoryFile = $leagueDir . "/$PARAM_category.json";

    if ( !file_exists($categoryFile) ) continue;

    $json = json_decode( file_get_contents( $categoryFile ) , true );

    foreach($json as $item) {
      if ( $item["index"] === $PARAM_index ) {
        $splitDir = explode("/", $leagueDir);
        $league = end( $splitDir );
        $payload[$league] = $item;
        break;
      }
    }
  }

  return $payload;
}

// Set header to json
header("Content-Type: application/json");

if (isset($_GET["category"])) {
  $PARAM_category = strtolower(trim(preg_replace("/[^A-Za-z]/", "", $_GET["category"])));
  if (!$PARAM_category || strlen($PARAM_category) > 32) {
    die("{\"error\": \"Invalid params\", \"field\": \"category\"}");
  }
} else die("{\"error\": \"Invalid params\", \"field\": \"category\"}");

if (isset($_GET["index"])) {
  $PARAM_index = preg_replace("/[^a-f0-9-]/", "", $_GET["index"]);
  if (!$PARAM_index || strlen($PARAM_index) != 7) {
    die("{\"error\": \"Invalid params\", \"field\": \"index\"}");
  }
} else die("{\"error\": \"Invalid params\", \"field\": \"index\"}");

// Test index integrity
$splitIndex = explode("-", $PARAM_index);
if (sizeof($splitIndex) !== 2 || strlen($splitIndex[0]) !== 4 || strlen($splitIndex[1]) !== 2) {
  die("{\"error\": \"Invalid params\", \"field\": \"index\"}");
}

$payload = array();

$payload["new"] = getNewHistory($PARAM_category, $PARAM_index);
$payload["old"] = getOldHistory($PARAM_category, $PARAM_index);
$payload["data"] = getPriceData($PARAM_category, $PARAM_index);

if ( empty($payload) ) {
  die("{\"error\": \"No results\"}");
} else {
  echo json_encode( $payload );
}
