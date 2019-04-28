library('ggplot2')
d = read.csv(paste("evaldata.csv",sep=""))
d = d[,-5]
d = d[complete.cases(d),]

domainstoplot = unique(d[d$Method == "ScrapingHub",]$Domain)


d = d[d$Domain %in% domainstoplot,]


d$Method <- factor(d$Method, 
       levels = c("XTPath",
                  "ScrapingHub"))


library(scales)
xtpathPalette <- c("#0072B2", "#D55E00")


ggplot(data = d, aes(Domain, fill=Method)) + 
  geom_bar(position="dodge", aes(y=d$Success/d$Total),stat="identity") +
  scale_fill_manual(values=xtpathPalette) +
  ylab("Percent Success") + xlab("Domain") + 
  theme(panel.background = element_blank()) + coord_flip() + theme(legend.position="bottom")
