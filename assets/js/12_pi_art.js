let piDigits;
let points = []; // stores 10 vectors (coordinates), one for each digit 0–9, arranged in a circle
let i = 0;
let iterations = 5000; // number of connections to draw
let baseRadius;
let angle;
let radii = []; // one radius per digit 0–9
let contractionStep = 0.27; // how much each digit moves inward per usage
let angles = []; // current angle for each digit
let maxFrameRate = 60;

function preload() {
  piDigits = loadStrings("/assets/other/pi.txt");
}

function getPoint(digit) {
  let angle = angles[digit]; // use the dynamic angle
  let r = radii[digit]; // current radius for this digit
  return createVector(cos(angle) * r, sin(angle) * r);
}

function setup() {
  createCanvas(windowWidth, windowHeight);
  colorMode(HSB, 360, 100, 100, 255);
  background(10);
  noFill();
  frameRate(2);

  // prepare pi digits
  piDigits = piDigits.join("");
  piDigits = piDigits.replace(/\D/g, "");
  iterations = min(5300, piDigits.length - 1);

  // Base radius and per-digit radii/angles
  baseRadius = min(width, height) * 0.38;
  for (let d = 0; d < 10; d++) {
    radii[d] = baseRadius; // starting radius (outer circumference)
    angles[d] = (TWO_PI * d) / 10 - HALF_PI; // starting angle for each digit
  }
}

function draw() {
  // Gradually increase speed
  if (i > 15) {
    // After the first 15 connections, ramp up speed linearly to maxFrameRate
    let newRate = map(i, 15, iterations, 2, maxFrameRate);
    newRate = constrain(newRate, 50, maxFrameRate);
    frameRate(newRate);
  }

  translate(windowWidth / 2, windowHeight / 2);

  if (i >= iterations) {
    noLoop();
    print("done");
    return;
  }

  // --- get current digits from pi ---
  let a = int(piDigits[i]);
  let b = int(piDigits[i + 1]);

  if (!isNaN(a) && !isNaN(b)) {
    // Rotate lines
    let globalAngleStep = 0.0005; // rotation amount in radians; tweak for rotation speed
    angles[a] -= globalAngleStep;
    angles[b] -= globalAngleStep;

    // Colors for start and end digits
    let hueA = map(a, 0, 9, 0, 360);
    let hueB = map(b, 0, 9, 0, 360);
    let colA = color(hueA, 65, 100, 10);
    let colB = color(hueB, 65, 100, 10);

    // Get current positions based on dynamic radius
    let ptStart = getPoint(a);
    let ptEnd = getPoint(b);

    // Draw straight line with gradient
    let segments = 200; // more segments = smoother gradient
    for (let t = 0; t <= 1; t += 1 / segments) {
      let prevX = lerp(ptStart.x, ptEnd.x, t);
      let prevY = lerp(ptStart.y, ptEnd.y, t);
      let nextX = lerp(ptStart.x, ptEnd.x, t + 1 / segments);
      let nextY = lerp(ptStart.y, ptEnd.y, t + 1 / segments);

      let c = lerpColor(colA, colB, t);
      stroke(c);
      strokeWeight(1);
      line(prevX, prevY, nextX, nextY);
    }

    // Contract radii of digits toward the center
    radii[a] = max(0, radii[a] - contractionStep);
    radii[b] = max(0, radii[b] - contractionStep);
  }

  i++;

  // function keyPressed() {
  // if (key === 's') {
  //   saveGif('pi_5300', 30);
  // }
  // }
}
