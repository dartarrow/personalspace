hold on
grid on
% Plot the Points Collected
plot3(botLeftCollectedZ, botLeftCollectedX, botLeftCollectedY, 'ro');
plot3(botRightCollectedZ, botRightCollectedX, botRightCollectedY, 'ro');
plot3(topRightCollectedZ, topRightCollectedX, topRightCollectedY, 'ro');
plot3(topLeftCollectedZ, topLeftCollectedX, topLeftCollectedY, 'ro');


%Plot the Clustered Points
plot3(botLeftClusterZ, botLeftClusterX, botLeftClusterY, 'b.');
plot3(botRightClusterZ, botRightClusterX, botRightClusterY, 'b.');
plot3(topRightClusterZ, topRightClusterX, topRightClusterY, 'b.');
plot3(topLeftClusterZ, topLeftClusterX, topLeftClusterY, 'b.');

%draw the matrix
fill3([topRightPointZ; topLeftPointZ; botLeftPointZ; botRightPointZ],[topRightPointX; topLeftPointX; botLeftPointX; botRightPointX],[topRightPointY; topLeftPointY; botLeftPointY; botRightPointY], 'k', 'FaceAlpha', 0.1);
