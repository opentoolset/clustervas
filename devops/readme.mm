<map version="1.0.1">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1573203463683" ID="ID_508455437" MODIFIED="1581779688661" STYLE="bubble" TEXT="Instructions for&#xa;building,&#xa;installation and&#xa;maintenance&#xa;">
<node CREATED="1581779964673" ID="ID_774357712" MODIFIED="1581779969277" POSITION="right" TEXT="ClusterVAS base image">
<node CREATED="1581782484500" ID="ID_301769440" MODIFIED="1581782650029" TEXT="for&#xa;GVM-10&#xa;GVMd-8">
<node COLOR="#990000" CREATED="1581779980948" ID="ID_1132843171" LINK="docker/clustervas/01-clustervas-base/gvm-10/" MODIFIED="1581782489788" TEXT="cd docker/clustervas/01-clustervas-base/gvm-10/"/>
<node COLOR="#990000" CREATED="1581780024761" ID="ID_1793806068" MODIFIED="1581782489787" TEXT="docker build -t clustervas-base ."/>
<node CREATED="1581782631525" ID="ID_187183263" MODIFIED="1581782672450" TEXT="Issues&#xa;related to&#xa;GVM-10&#xa;GVMd-8&#xa;...">
<node CREATED="1581782522052" ID="ID_1657986078" MODIFIED="1581782637381" TEXT="Long running SCAP synchronization">
<node COLOR="#990000" CREATED="1581782561193" ID="ID_917126584" MODIFIED="1581782593169" TEXT="ps -ef | grep gvm">
<node COLOR="#338800" CREATED="1581782588053" ID="ID_512019762" MODIFIED="1581782592535" TEXT="...&#xa;gvmd: Syncing SCAP&#xa;..."/>
</node>
<node CREATED="1581782509771" ID="ID_709991897" LINK="https://github.com/greenbone/gvmd/issues/822" MODIFIED="1581782557579" TEXT="https://github.com/greenbone/gvmd/issues/822"/>
<node CREATED="1581782683331" LINK="https://community.greenbone.net/t/gvmd-8-scap-update-takes-a-looong-time/3302" MODIFIED="1581782683331" TEXT="https://community.greenbone.net/t/gvmd-8-scap-update-takes-a-looong-time/3302"/>
<node CREATED="1581782735958" LINK="https://community.greenbone.net/t/proctitle-gvmd-syncing-scap/2427" MODIFIED="1581782735958" TEXT="https://community.greenbone.net/t/proctitle-gvmd-syncing-scap/2427"/>
</node>
</node>
</node>
</node>
<node CREATED="1581780069917" ID="ID_408952801" MODIFIED="1581780159267" POSITION="right" TEXT="ClusterVAS image">
<node COLOR="#990000" CREATED="1581779980948" ID="ID_799179947" LINK="docker/clustervas/02-clustervas-template/" MODIFIED="1590386907292" TEXT="cd docker/clustervas/02-clustervas-template/"/>
<node COLOR="#990000" CREATED="1581780024761" ID="ID_1752200678" MODIFIED="1590386853774" TEXT="docker build -t clustervas-template ."/>
</node>
<node CREATED="1581780196760" ID="ID_1488740929" MODIFIED="1581780538846" POSITION="right" TEXT="Starting">
<node CREATED="1581780500637" ID="ID_1968469485" MODIFIED="1581780542994" TEXT="ClusterVAS template container">
<node COLOR="#990000" CREATED="1581779980948" ID="ID_426801818" LINK="docker/clustervas/" MODIFIED="1581780533296" TEXT="cd docker/clustervas/"/>
<node COLOR="#990000" CREATED="1581780474401" ID="ID_349072799" MODIFIED="1581780533295" TEXT="mkdir -pv data"/>
<node COLOR="#990000" CREATED="1581780480084" ID="ID_309166441" LINK="docker/clustervas/start-clustervas-template.sh" MODIFIED="1590386973817" STYLE="bubble" TEXT="./start-clustervas-template.sh"/>
</node>
</node>
<node CREATED="1573476902183" ID="ID_241215288" MODIFIED="1581622774071" POSITION="right" TEXT="Test">
<node COLOR="#990000" CREATED="1581781701450" ID="ID_982523568" MODIFIED="1581781712173" TEXT="docker attach clustervas-template"/>
<node CREATED="1581781723872" ID="ID_872229223" MODIFIED="1581781735461" TEXT="Container shell">
<node COLOR="#990000" CREATED="1573476998289" ID="ID_1966892277" MODIFIED="1581622774071" TEXT="gvm-cli socket --xml &quot;&lt;get_version /&gt;&quot;">
<node COLOR="#338800" CREATED="1573477009887" ID="ID_861315474" MODIFIED="1581622774072" TEXT="&lt;get_version_response status=&quot;200&quot; status_text=&quot;OK&quot;&gt;&lt;version&gt;8.0&lt;/version&gt;&lt;/get_version_response&gt; "/>
</node>
<node COLOR="#990000" CREATED="1573476942209" ID="ID_424267135" MODIFIED="1581622774072" TEXT="gvm-cli socket --xml &quot;&lt;commands /&gt;&quot;"/>
<node COLOR="#990000" CREATED="1573476964452" ID="ID_1715982608" MODIFIED="1581622774072" TEXT="gvm-cli socket --gmp-username admin --gmp-password admin&#xa;&lt;get_configs /&gt;"/>
<node COLOR="#990000" CREATED="1573476976647" ID="ID_798336136" MODIFIED="1581622774075" TEXT="gvm-cli socket --xml &quot;&lt;commands&gt;&lt;authenticate&gt;&lt;credentials&gt;&lt;username&gt;admin&lt;/username&gt;&lt;password&gt;admin&lt;/password&gt;&lt;/credentials&gt;&lt;/authenticate&gt;&lt;get_configs /&gt;&lt;/commands&gt;&quot;"/>
<node CREATED="1581781742050" ID="ID_1981187945" MODIFIED="1581781768314" TEXT="CTRL+P and CTRL+Q"/>
</node>
</node>
</node>
</map>
