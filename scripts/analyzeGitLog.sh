# 分析最稳定的文件，设计的很棒 or 比较冷门？
#git log --since="2021-01-01 01:30:00" --until="2023-09-20 01:30:00" --pretty=format: --name-only | sort | uniq -c | sed "s/.*pom.xml//g" | sed "s/.*drone.yml//g" | sed "s/.*spring.factories//g" | sort -rg | tail -20

# 分析最不稳定的文件，为什么频繁变更？可能不适合在框架？业界更新热门？
git log --since="2023-01-01 00:00:00" --until="2024-04-01 00:00:00" --pretty=format: --name-only | sort | uniq -c  | sed "s/.*pom.xml//g" | sed "s/.*drone.yml//g" | sed "s/.*spring.factories//g" | sort -rg | head -30
