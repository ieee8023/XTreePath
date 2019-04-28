
library('ggplot2')
dataset = "A"
#dataraw = read.csv(paste("data2plot/varyTrainRatio.csv",sep=""))
dataraw = read.csv(paste("data2plot/varyTrainRatio100.csv",sep=""))

dataraw100 = dataraw100[complete.cases(dataraw100),]

#dataall = merge(dataraw,dataraw100, all=TRUE)






XTPathF1 = data.frame(dataraw$DATASET,dataraw$TRAIN, dataraw$xpathTreeArrayF,c("XTPath"), c("F1"))
colnames(XTPathF1) = c("Dataset","Training", "Value","Method", "Metric")
XTPathP = data.frame(dataraw$DATASET,dataraw$TRAIN, dataraw$xpathTreeArrayP,c("XTPath"), c("P"))
colnames(XTPathP) = c("Dataset","Training", "Value","Method", "Metric")
XTPathR = data.frame(dataraw$DATASET,dataraw$TRAIN, dataraw$xpathTreeArrayR,c("XTPath"), c("R"))
colnames(XTPathR) = c("Dataset","Training", "Value","Method", "Metric")


XPathF1 = data.frame(dataraw$DATASET,dataraw$TRAIN, dataraw$xpathF,c("XPath"), c("F1"))
colnames(XPathF1) = c("Dataset","Training", "Value","Method", "Metric")
XPathP = data.frame(dataraw$DATASET,dataraw$TRAIN, dataraw$xpathP,c("XPath"), c("P"))
colnames(XPathP) = c("Dataset","Training", "Value","Method", "Metric")
XPathR = data.frame(dataraw$DATASET,dataraw$TRAIN, dataraw$xpathR,c("XPath"), c("R"))
colnames(XPathR) = c("Dataset","Training", "Value","Method", "Metric")

TPathF1 = data.frame(dataraw$DATASET,dataraw$TRAIN, dataraw$treeArrayF,c("TreePath"), c("F1"))
colnames(TPathF1) = c("Dataset","Training", "Value","Method", "Metric")
TPathP = data.frame(dataraw$DATASET,dataraw$TRAIN, dataraw$treeArrayP,c("TreePath"), c("P"))
colnames(TPathP) = c("Dataset","Training", "Value","Method", "Metric")
TPathR = data.frame(dataraw$DATASET,dataraw$TRAIN, dataraw$treeArrayR,c("TreePath"), c("R"))
colnames(TPathR) = c("Dataset","Training", "Value","Method", "Metric")

verticallongf1 = rbind(XTPathF1,XPathF1, TPathF1)


verticallong = rbind(XTPathF1, XTPathP, XTPathR,
                         XPathF1, XPathP, XPathR,
                      TPathF1, TPathP, TPathR)

Method = verticallongf1$Method













verticallong$Method <- factor(verticallong$Method, 
                   levels = c("XPath",
                              "XTPath",
                              "TreePath"))


width=450
height=230
xliml = 0.01
xlimh = 1.0
png(filename=paste("data2plot/varytrainRatioF",dataset,".png",sep=""),width=width, height=height)

xtpathPalette <- c("#D55E00","#0072B2","#0072A2")
d = verticallong[verticallong$Metric=="F1",]
ggplot(data = d, 
       aes(x=d$Training, 
           color=Method,
           linetype = Method)) + 
  geom_smooth(aes(y=d$Value), se = FALSE) + 
  scale_color_manual(values=xtpathPalette) +
  ylab("F1-Score") + xlab("Precentage Trained") +
  theme(panel.background = element_blank())




dev.off()



png(filename=paste("data2plot/varytrainRatioP",dataset,".png",sep=""),width=width, height=height)

d = verticallong[verticallong$Metric=="P",]
ggplot(data = d, 
       aes(x=d$Training, 
           color=Method,
           linetype = Method)) + 
  stat_smooth(aes(y=d$Value), se = FALSE) + 
  scale_color_manual(values=xtpathPalette) +
  ylab("Precision") + xlab("Precentage Trained") +
  theme(panel.background = element_blank())



dev.off()



png(filename=paste("data2plot/varytrainRatioR",dataset,".png",sep=""),width=width, height=height)


d = verticallong[verticallong$Metric=="R",]
ggplot(data = d, 
       aes(x=d$Training, 
           color=Method,
           linetype = Method)) + 
  stat_smooth(aes(y=d$Value), se = FALSE, method = "loess") + 
  scale_color_manual(values=xtpathPalette) +
  ylab("Recall") + xlab("Precentage Trained")+
  theme(panel.background = element_blank())

dev.off()






















datavert = aggregate(dataraw,by=list(dataraw$DATASET), FUN=mean)


ggplot(data = datavert, aes(x=datavert$Group.1)) + 
  geom_bar(aes(y=datavert$xpathTreeArrayR), stat="identity")





ylimj = ylim(0.2,1)

datad = aggregate(dataraw,by=list(dataraw$TRAIN, dataraw$DATASET), FUN=mean)


png(filename=paste("data2plot/varytrainRatioF-Each",dataset,".png",sep=""),width=width, height=height)

ggplot(data = datad, aes(x=datad$Group.1, color="Method")) + 
  geom_line(aes(y=xpathF, color=datad$Group.2)) + 
  ylab("F1-Score") + xlab("Precentage Trained") + xlim(xliml,xlimh) + ylimj
#  geom_line(aes(y=data$treeArrayF, color="TPath")) + 
#  geom_line(aes(y=data$xpathF, color="XPath"))

dev.off()


png(filename=paste("data2plot/varytrainRatioP-Each",dataset,".png",sep=""),width=width, height=height)

ggplot(data = datad, aes(x=datad$Group.1, color="Method")) + 
  geom_line(aes(y=xpathP, color=datad$Group.2)) + 
  ylab("Precision") + xlab("Precentage Trained") + xlim(xliml,xlimh) + ylimj
#  geom_line(aes(y=data$treeArrayF, color="TPath")) + 
#  geom_line(aes(y=data$xpathF, color="XPath"))

dev.off()


png(filename=paste("data2plot/varytrainRatioR-Each",dataset,".png",sep=""),width=width, height=height)

ggplot(data = datad, aes(x=datad$Group.1, color="Method")) + 
  geom_line(aes(y=xpathR, color=datad$Group.2)) + 
  ylab("Recall") + xlab("Precentage Trained") + xlim(xliml,xlimh) + ylimj
#  geom_line(aes(y=data$treeArrayF, color="TPath")) + 
#  geom_line(aes(y=data$xpathF, color="XPath"))

dev.off()




#  geom_smooth(aes(y=xpathTreeArrayF, colour=data$DATASET, group=data$DATASET), 
#              level=0.2, method = "average")
#  scale_colour_discrete(name  ="Dataset") +
#  scale_x_discrete(breaks = c(0.02,0.05, 0.1,0.15,0.2)) + 
#  








