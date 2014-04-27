function [idVec] = index_of_difficulty(distanceVec, widthVec)
%boxplot((distanceVec./widthVec)+1)
idVec=log2((distanceVec./widthVec)+1);
end
