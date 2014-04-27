%gets mean, median, stdDev, iQR, and variance of throughput per log
%parameters are the logfile => 'expLog_xxxx_xxxxx', distanceType =>
%'disappear(widgetAppear to disappear), or
%shortestPath(widgetAppear-MouseEnter) 
% the final parameter is the division coefficient for the effective width,
% n \in 0-1 ,  n * box size 1 being the whole box, anything between 0 and 1
% being a percentage of the box to use as the effective width
function [avgTP, medianTP, standardDeviation, interQuartileRange, variance] =  getTPandID (logFile, distanceType, effectiveWidthCoeff) 
    run(logFile);
    if strcmp(distanceType,'disappear')%widgetDisappear-widgetAppear
        widgetDisappearPosX;
        distance=sqrt((widgetDisappearPosX-widgetAppearPosX).^2+(widgetDisappearPosY-widgetAppearPosY).^2);
    
    elseif strcmp(distanceType,'shortestPath') %MouseEnter-widgetAppear
            distance=shortestPath;
    end
    distance;
    
    
    width=sqrt(SizePerTrial)*effectiveWidthCoeff; %sqrt because we have area of a square so we have height*width=area so we need sqrt(area) for width
    indexODifficulty=index_of_difficulty(distance, width);
   
    
    % travelDuration is measured in milliseconds and is always negative as
    % we subtracted the wrong way. Througput is measured in seconds using the shannon forumlation
    % by Mackenzies 27 years of fitts paper's recommendation
    throughputPerTrial=indexODifficulty./abs(travelDuration /1000); 
    avgTP=mean(throughputPerTrial);
    medianTP=median(throughputPerTrial);
    
    standardDeviation=std(throughputPerTrial);
    interQuartileRange=iqr(throughputPerTrial);
    variance=standardDeviation^2;
    
    
    %figure(1)
    %hold on;
    %boxplot(throughputPerTrial)
    %plot(throughputPerTrial, 'b-');
    %hold off;
    %figure(2)
    %hold on;
    %scatter(ones(size(indexODifficulty)), indexODifficulty);
    %boxplot(indexODifficulty)
    %hold off;
end
