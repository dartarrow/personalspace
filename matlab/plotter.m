% need to rotate this graph so that it matches the users space and not just some mathematical space
plot3(Z, X, Y)

% the labels are also rotated
xlabel('Z space')
ylabel('X space')
zlabel('Y space')

% prettify the graph area
grid on
% sets the aspect ratio so that the data units are the same in every direction
axis equal

