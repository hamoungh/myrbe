function rate=workload()
            subplot_ = 0;
            steps = 120;
            
            % import workload and devide it to applications (2h for each of
            % 3 apps) 
            load('./dataset/workload_interarr_6h_min_final.mat', 'times','intarr', '-mat');   
%             w(:,1)=intarr(1         :1*118)'.* 14;
%             w(:,2)=intarr(118+1     :2*118)'.* 11;
%             w(:,3)=intarr(2*118+1   :3*118)'.* 8;

            w(:,1)=intarr(1         :1*118)';
            w(:,2)=intarr(118+1     :2*118)';
            w(:,3)=intarr(2*118+1   :3*118)';

            
%             w = [sim.sinus_workload2(22,44,10,1, steps), ...
%                 sim.sinus_workload2(22,44,10,2, steps),...
%                 sim.sinus_workload2(22,44,10,4, steps)];
            
            if subplot_; handle=figure; end;
            color = {'r' 'b' 'g'}; % consequtively for apps
            
            %%%%%%%%%%%%% plot workloads %%%%%%%%%%%%%%
            if subplot_; subplot(3,2,1); else; figure; end;
            % plot(res.w)            
            file_1 = fopen('workload.txt','w')
            w(:,2) = w(:,2)*.7;
            for ii=1:3
                rate(:,ii) = 400./w(:,ii);
                rate2(:,ii)= [(1:rate(1,ii)/6:rate(1,ii))' ; rate(:,ii)]
                plot(rate2(:,ii) , strcat(color{ii},'-'));
                hold on;
                str = vect2str(rate2(:,ii), 'formatstring', '%0.0f','openingDelimiter',' ','closingDelimiter',' ','separator',' ')
                fprintf(file_1,'workload= %s\n',str)
            end
            fclose(file_1)
            title('Workloads');
            xlabel('Time');
            ylabel('Arrival Rate(\lambda)');
            legend('app1','app2','app3',3);
            
            
            
end

%workload()