let wildFlowers1;
let imgWidth;
let imgHeight;
let imageCount;

let garden;
let drawsPerFrame;
let densityFactor = 0.2; // Increase for faster fill

function preload() {
  wildFlowers1 = loadImage("assets/wildflowers_pinterest_drawing.jpg");
}

function setup() {
  createCanvas(windowWidth, windowHeight);
  garden = createGraphics(windowWidth, windowHeight);

  // Scale image to fill canvas height while preserving aspect ratio
  let aspectRatio = wildFlowers1.width / wildFlowers1.height;
  imgHeight = windowHeight;
  imgWidth = imgHeight * aspectRatio;

  // Compute how many tiles are needed to fill the full width
  imageCount = ceil(windowWidth / imgWidth);

  // Scaled drawing effort based on canvas size
  drawsPerFrame = int(width * height * densityFactor);

  background(255);
  frameRate(30);
}

  // Clustered bloom - multiple clusters per frame
  function draw() {
    image(garden, 0, 0);
  
    // Draw every frame (no skip)
    let clusterCount = 50; // Spread across tiles
    let clusterSize = 1;   // Burst per cluster
  
    for (let i = 0; i < clusterCount; i++) {
      let j = floor(random(imageCount));
      for (let k = 0; k < clusterSize; k++) {
        paintFlowers(wildFlowers1, j * imgWidth);
      }
    }
  }
  

  // Clustered bloom - One cluster per frame
// function draw() {
//   image(garden, 0, 0);
  
//   let j = floor(random(imageCount));
//   let clusterSize = floor(random(3, 27));
//   for (let k = 0; k < clusterSize; k++) {
//     paintFlowers(wildFlowers1, j * imgWidth);
//   }
// }

// One dot per frame
// let j = floor(random(imageCount));
// paintFlowers(wildFlowers1, j * imgWidth);

  // Loop over each tile and draw random points from the image
//   for (let i = 0; i < drawsPerFrame; i++) {
//     for (let j = 0; j < imageCount; j++) {
//       paintFlowers(wildFlowers1, j * imgWidth);
//     }
//   }
// }

function paintFlowers(img, offsetX) {
  // Choose a random pixel from the image
  let sourceX = floor(random(img.width));
  let sourceY = floor(random(img.height));
  let c = img.get(sourceX, sourceY);

  // Map image coords to canvas coords
  let drawX = map(sourceX, 0, img.width, offsetX, offsetX + imgWidth);
  let drawY = map(sourceY, 0, img.height, 0, imgHeight);

  // Draw to the garden layer
  garden.stroke(c);
  garden.strokeWeight(random(1, 10));
  garden.point(drawX, drawY);

}
