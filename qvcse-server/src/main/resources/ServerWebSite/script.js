function show(elmnt){
    if (document.getElementById(elmnt)===null) {
        return;
    }
    document.getElementById(elmnt).style.marginLeft=0+'px';
    document.getElementById(elmnt).style.display="block";
}
function hide(elmnt){
    if (document.getElementById(elmnt)===null) {
        return;
    }
    document.getElementById(elmnt).style.display="none";
}
function menuInit(){
    document.getElementById('menu').style.display="block";
    menuWrite();
    x1=eval(0);
    window.scrollBy(0,1);
    x2=eval(0);
    window.scrollBy(0,-1);
    if ((x2-x1)<1){
        scrolled='document.documentElement.scrollTop';
    }
}
function subMenuInit(){
    document.getElementById('menu').style.display="block";
    subMenuWrite();
    x1=eval(0);
    window.scrollBy(0,1);
    x2=eval(0);
    window.scrollBy(0,-1);
    if ((x2-x1)<1){
        scrolled='document.documentElement.scrollTop';
    }
}

function menuWrite(){
    ff ='';
    ff+='<ul class="top" id="top">';
    ff+='<li class="top"><a class="toph" href="index.html">Home<\/a><\/li>';
    ff+='<li class="top" onmouseover="show(\'tutorial\')" onmouseout="hide(\'tutorial\')">';
    ff+='<a class="top" href="">Tutorials<\/a>';
    ff+='<ul class="list" id="tutorial" onmouseover="show(\'tutorial\')" onmouseout="hide(\'tutorial\')">';
    ff+='<li class="list"><a class="list" href="getstarted/readme1.html">Getting Started<\/a><\/li>';
    ff+='<li class="list"><a class="list" href="getstarted/clientbasics1.html">Client Application Basics<\/a><\/li>';
    ff+='<\/ul><\/li>';
    ff+='<li class="top" onmouseover="show(\'doc\')" onmouseout="hide(\'doc\')">';
    ff+='<a class="top" href="docs/intro.html">Documentation<\/a>';
    ff+='<ul class="list" id="doc" onmouseover="show(\'doc\')" onmouseout="hide(\'doc\')">';
    ff+='<li class="list"><a class="list" href="getstarted/readme1.html">Installation<\/a><\/li>';
    ff+='<li class="list"><a class="list" href="docs/faq.html">FAQ<\/a><\/li>';
    ff+='<li class="list"><a class="list" href="docs/qvcsanttask.html">QVCS Custom Ant Task<\/a><\/li>';
    ff+='<li class="list" onmouseover="show(\'understand\')" onmouseout="hide(\'understand\')">';
    ff+='<a class="list" href="">Understanding...<\/a>';
    ff+='<ul class="sublist" id="understand" onmouseover="show"(\'understand\')" onmouseout="hide(\'understand\')">';
    ff+='<li class="sublist"><a class="list" href="docs/understandingClientAPI.html">Client API<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/understandingToolbar.html">Toolbar<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/understandingFileStatus.html">File Status<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/understandingFilters.html">Filters<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/understandingMerge.html">Merge<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/understandingDirectories.html">Directory Usage<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/understandingQVCSAttributes.html">QVCS Attributes<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/understandingLabels.html">Labels<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/understandingViews.html">Views<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/understandingAutomaticUpdates.html">Automatic Updates<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/understandingRolesAndActions.html">Roles and Actions<\/a><\/li>';
    ff+='<\/ul><\/li>';
    ff+='<li class="list" onmouseover="show(\'reference\')" onmouseout="hide(\'reference\')">';
    ff+='<a class="list" href="">Reference...<\/a>';
    ff+='<ul class="sublist" id="reference" onmouseover="show"(\'reference\')" onmouseout="hide(\'reference\')">';
    ff+='<li class="sublist"><a class="list" href="clientAPIDocs/index.html">Client API<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/referenceServerCommandLine.html">Server Command Line<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/referenceClientCommandLine.html">Client Command Line<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/referenceAdminCommandLine.html">Admin Command Line<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/referenceUserPreferencesDialog.html">User Preferences Dialog<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/referenceFileGroupsDialog.html">Define File Groups Dialog<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="docs/understandingFilters.html">Filters<\/a><\/li>';
    ff+='<\/ul><\/li>';
    ff+='<li class="list"><a class="list" href="docs/glossary.html">Glossary<\/a><\/li>';
    ff+='<li class="list"><a class="list" href="docs/license.html">License<\/a><\/li>';
    ff+='<li class="list"><a class="list" href="docs/acknowledgements.html">Acknowledgements<\/a><\/li>';
    ff+='<\/ul><\/li>';
    ff+='<li class="top"><a class="top" href="new.html">What\'s New<\/a><\/li>';
    ff+='<li class="top"><a class="top" href="features.html">Features<\/a><\/li>';
    ff+='<li class="top"><a class="top" href="screen.html">Screenshot<\/a><\/li>';
    ff+='<\/ul>';


    ie ='';
    ie+='<ul class="etop" id="top"><li class="top"><a class="etop" href="index.html"><center> Home <\/center><\/a><\/li>';
    ie+='<li class="etop" onmouseover="show(\'tutorial\')" onmouseout="hide(\'tutorial\')">';
    ie+='<a class="etop" href="#"><center>Tutorials<\/center><\/a>';
    ie+='<ul class="elist" id="tutorial" onmouseover="show(\'tutorial\')" onmouseout="hide(\'tutorial\')">';
    ie+='<li class="efirst"><a class="list" href="getstarted/readme1.html">Getting Started<\/a><\/li>';
    ie+='<li class="elist"><a class="list" href="getstarted/clientbasics1.html">Client App Basics<\/a><\/li>';
    ie+='<\/ul><\/li>';
    ie+='<li class="etop" onmouseover="show(\'doc\')" onmouseout="hide(\'doc\')">';
    ie+='<a class="etop" href="docs/intro.html"><center>Documentation<\/center><\/a>';
    ie+='<ul class="elist" id="doc" onmouseover="show(\'doc\')" onmouseout="hide(\'doc\')">';
    ie+='<li class="efirst"><a class="list" href="getstarted/readme1.html">Installation<\/a><\/li>';
    ie+='<li class="elist"><a class="list" href="docs/faq.html">FAQ<\/a><\/li>';
    ie+='<li class="elist"><a class="list" href="docs/qvcsanttask.html">QVCS Custom Ant Task<\/a><\/li>';
    ie+='<li class="elist" onmouseover="show(\'understand\')" onmouseout="hide(\'understand\')">';
    ie+='<a class="non" href="#">Understanding...<\/a>';
    ie+='<ul class="esublist" id="understand" onmouseover="show"(\'understand\')" onmouseout="hide(\'understand\')">';
    ie+='<li class="esubfirst"><a class="list" href="docs/understandingClientAPI.html">Client API<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/understandingToolbar.html">Toolbar<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/understandingFileStatus.html">File Status<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/understandingFilters.html">Filters<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/understandingMerge.html">Merge<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/understandingDirectories.html">Directory Usage<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/understandingQVCSAttributes.html">QVCS Attributes<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/understandingLabels.html">Labels<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/understandingViews.html">Views<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/understandingAutomaticUpdates.html">Automatic Updates<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/understandingRolesAndActions.html">Roles and Actions<\/a><\/li>';
    ie+='<\/ul><\/li>';
    ie+='<li class="elist" onmouseover="show(\'reference\')" onmouseout="hide(\'reference\')">';
    ie+='<a class="non" href="#">Reference...<\/a>';
    ie+='<ul class="esublist" id="reference" onmouseover="show"(\'reference\')" onmouseout="hide(\'reference\')">';
    ie+='<li class="esubfirst"><a class="list" href="clientAPIDocs/index.html">Client API<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/referenceServerCommandLine.html">Server Command Line<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/referenceClientCommandLine.html">Client Command Line<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/referenceAdminCommandLine.html">Admin Command Line<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/referenceUserPreferencesDialog.html">User Preferences Dialog<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/referenceFileGroupsDialog.html">Define File Groups Dialog<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="docs/understandingFilters.html">Filters<\/a><\/li>';
    ie+='<\/ul><\/li>';
    ie+='<li class="elist"><a class="list" href="docs/glossary.html">Glossary<\/a><\/li>';
    ie+='<li class="elist"><a class="list" href="docs/license.html">License<\/a><\/li>';
    ie+='<li class="elist"><a class="list" href="docs/acknowledgements.html">Acknowledgements<\/a><\/li>';
    ie+='<\/ul><\/li>';
    ie+='<li class="etop"><a class="etop" href="new.html"><center>What\'s New<\/center><\/a><\/li>';
    ie+='<li class="etop"><a class="etop" href="features.html"><center>Features<\/center><\/a><\/li>';
    ie+='<li class="etop"><a class="etop" href="screen.html"><center>Screenshot<\/center><\/a><\/li>';
    ie+='<\/ul>';

    browsername=navigator.appName;
    if (browsername.indexOf("Netscape")!==-1) {
        browsername="NS";
    }
    else
    {
        if (browsername.indexOf("Microsoft")!==-1) {
            browsername="MSIE";
        }
        else {
            browsername="N/A";
        }
    }

    vendor=navigator.vendor;
    if (vendor!==null) {
        if (vendor.indexOf("Apple")!==-1) {
            browsername="Safari";
        }
        if (vendor.indexOf("Google")!==-1) {
            browsername="Chrome";
        }
    }

    browserversion="0";
    if (navigator.appVersion.indexOf("2.")!==-1) {
        browserversion="2";
    }
    if (navigator.appVersion.indexOf("3.")!==-1) {
        browserversion="3";
    }
    if (navigator.appVersion.indexOf("4.")!==-1) {
        browserversion="4";
    }
    if (navigator.appVersion.indexOf("5.")!==-1) {
        browserversion="5";
    }
    if (navigator.appVersion.indexOf("6.")!==-1) {
        browserversion="6";
    }

    if (browsername==="NS"||browsername==="Safari"||browsername==="Chrome") {
        document.getElementById('zone').innerHTML=ff;
    }
    else if (browsername==="MSIE"){
        if (browserversion<=7){
            document.getElementById('zone').innerHTML=ie;
        }
        else {
            document.getElementById('zone').innerHTML=ff;
        }
    }
    if (browsername==="N/A") {
        document.getElementById('zone').innerHTML=ff;
    }
}



function subMenuWrite(){
    ff ='';
    ff+='<ul class="top" id="top">';
    ff+='<li class="top"><a class="toph" href="../index.html">Home<\/a><\/li>';
    ff+='<li class="top" onmouseover="show(\'tutorial\')" onmouseout="hide(\'tutorial\')">';
    ff+='<a class="top" href="">Tutorials<\/a>';
    ff+='<ul class="list" id="tutorial" onmouseover="show(\'tutorial\')" onmouseout="hide(\'tutorial\')">';
    ff+='<li class="list"><a class="list" href="../getstarted/readme1.html">Getting Started<\/a><\/li>';
    ff+='<li class="list"><a class="list" href="../getstarted/clientbasics1.html">Client Application Basics<\/a><\/li>';
    ff+='<\/ul><\/li>';
    ff+='<li class="top" onmouseover="show(\'doc\')" onmouseout="hide(\'doc\')">';
    ff+='<a class="top" href="../docs/intro.html">Documentation<\/a>';
    ff+='<ul class="list" id="doc" onmouseover="show(\'doc\')" onmouseout="hide(\'doc\')">';
    ff+='<li class="list"><a class="list" href="../getstarted/readme1.html">Installation<\/a><\/li>';
    ff+='<li class="list"><a class="list" href="../docs/faq.html">FAQ<\/a><\/li>';
    ff+='<li class="list"><a class="list" href="../docs/qvcsanttask.html">QVCS Custom Ant Task<\/a><\/li>';
    ff+='<li class="list" onmouseover="show(\'understand\')" onmouseout="hide(\'understand\')">';
    ff+='<a class="list" href="">Understanding...<\/a>';
    ff+='<ul class="sublist" id="understand" onmouseover="show"(\'understand\')" onmouseout="hide(\'understand\')">';
    ff+='<li class="sublist"><a class="list" href="../docs/understandingClientAPI.html">Client API<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/understandingToolbar.html">Toolbar<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/understandingFileStatus.html">File Status<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/understandingFilters.html">Filters<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/understandingMerge.html">Merge<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/understandingDirectories.html">Directory Usage<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/understandingQVCSAttributes.html">QVCS Attributes<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/understandingLabels.html">Labels<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/understandingViews.html">Views<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/understandingAutomaticUpdates.html">Automatic Updates<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/understandingRolesAndActions.html">Roles and Actions<\/a><\/li>';
    ff+='<\/ul><\/li>';
    ff+='<li class="list" onmouseover="show(\'reference\')" onmouseout="hide(\'reference\')">';
    ff+='<a class="list" href="">Reference...<\/a>';
    ff+='<ul class="sublist" id="reference" onmouseover="show"(\'reference\')" onmouseout="hide(\'reference\')">';
    ff+='<li class="sublist"><a class="list" href="../clientAPIDocs/index.html">Client API<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/referenceServerCommandLine.html">Server Command Line<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/referenceClientCommandLine.html">Client Command Line<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/referenceAdminCommandLine.html">Admin Command Line<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/referenceUserPreferencesDialog.html">User Preferences Dialog<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/referenceFileGroupsDialog.html">Define File Groups Dialog<\/a><\/li>';
    ff+='<li class="sublist"><a class="list" href="../docs/understandingFilters.html">Filters<\/a><\/li>';
    ff+='<\/ul><\/li>';
    ff+='<li class="list"><a class="list" href="../docs/glossary.html">Glossary<\/a><\/li>';
    ff+='<li class="list"><a class="list" href="../docs/license.html">License<\/a><\/li>';
    ff+='<li class="list"><a class="list" href="../docs/acknowledgements.html">Acknowledgements<\/a><\/li>';
    ff+='<\/ul><\/li>';
    ff+='<li class="top"><a class="top" href="../new.html">What\'s New<\/a><\/li>';
    ff+='<li class="top"><a class="top" href="../features.html">Features<\/a><\/li>';
    ff+='<li class="top"><a class="top" href="../screen.html">Screenshot<\/a><\/li>';
    ff+='<\/ul>';


    ie ='';
    ie+='<ul class="etop" id="top"><li class="top"><a class="etop" href="../index.html"><center> Home <\/center><\/a><\/li>';
    ie+='<li class="etop" onmouseover="show(\'tutorial\')" onmouseout="hide(\'tutorial\')">';
    ie+='<a class="etop" href="#"><center>Tutorials<\/center><\/a>';
    ie+='<ul class="elist" id="tutorial" onmouseover="show(\'tutorial\')" onmouseout="hide(\'tutorial\')">';
    ie+='<li class="efirst"><a class="list" href="../getstarted/readme1.html">Getting Started<\/a><\/li>';
    ie+='<li class="elist"><a class="list" href="../getstarted/clientbasics1.html">Client App Basics<\/a><\/li>';
    ie+='<\/ul><\/li>';
    ie+='<li class="etop" onmouseover="show(\'doc\')" onmouseout="hide(\'doc\')">';
    ie+='<a class="etop" href="../docs/intro.html"><center>Documentation<\/center><\/a>';
    ie+='<ul class="elist" id="doc" onmouseover="show(\'doc\')" onmouseout="hide(\'doc\')">';
    ie+='<li class="efirst"><a class="list" href="../getstarted/readme1.html">Installation<\/a><\/li>';
    ie+='<li class="elist"><a class="list" href="../docs/faq.html">FAQ<\/a><\/li>';
    ie+='<li class="elist"><a class="list" href="../docs/qvcsanttask.html">QVCS Custom Ant Task<\/a><\/li>';
    ie+='<li class="elist" onmouseover="show(\'understand\')" onmouseout="hide(\'understand\')">';
    ie+='<a class="non" href="#">Understanding...<\/a>';
    ie+='<ul class="esublist" id="understand" onmouseover="show"(\'understand\')" onmouseout="hide(\'understand\')">';
    ie+='<li class="esubfirst"><a class="list" href="../docs/understandingClientAPI.html">Client API<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/understandingToolbar.html">Toolbar<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/understandingFileStatus.html">File Status<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/understandingFilters.html">Filters<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/understandingMerge.html">Merge<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/understandingDirectories.html">Directory Usage<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/understandingQVCSAttributes.html">QVCS Attributes<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/understandingLabels.html">Labels<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/understandingViews.html">Views<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/understandingAutomaticUpdates.html">Automatic Updates<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/understandingRolesAndActions.html">Roles and Actions<\/a><\/li>';
    ie+='<\/ul><\/li>';
    ie+='<li class="elist" onmouseover="show(\'reference\')" onmouseout="hide(\'reference\')">';
    ie+='<a class="non" href="#">Reference...<\/a>';
    ie+='<ul class="esublist" id="reference" onmouseover="show"(\'reference\')" onmouseout="hide(\'reference\')">';
    ie+='<li class="esubfirst"><a class="list" href="../clientAPIDocs/index.html">Client API<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/referenceServerCommandLine.html">Server Command Line<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/referenceClientCommandLine.html">Client Command Line<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/referenceAdminCommandLine.html">Admin Command Line<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/referenceUserPreferencesDialog.html">User Preferences Dialog<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/referenceFileGroupsDialog.html">Define File Groups Dialog<\/a><\/li>';
    ie+='<li class="esublist"><a class="list" href="../docs/understandingFilters.html">Filters<\/a><\/li>';
    ie+='<\/ul><\/li>';
    ie+='<li class="elist"><a class="list" href="../docs/glossary.html">Glossary<\/a><\/li>';
    ie+='<li class="elist"><a class="list" href="../docs/license.html">License<\/a><\/li>';
    ie+='<li class="elist"><a class="list" href="../docs/acknowledgements.html">Acknowledgements<\/a><\/li>';
    ie+='<\/ul><\/li>';
    ie+='<li class="etop"><a class="etop" href="../new.html"><center>What\'s New<\/center><\/a><\/li>';
    ie+='<li class="etop"><a class="etop" href="../features.html"><center>Features<\/center><\/a><\/li>';
    ie+='<li class="etop"><a class="etop" href="../screen.html"><center>Screenshot<\/center><\/a><\/li>';
    ie+='<\/ul>';

    browsername=navigator.appName;
    if (browsername.indexOf("Netscape")!==-1) {
        browsername="NS";
    }
    else
    {
        if (browsername.indexOf("Microsoft")!==-1) {
            browsername="MSIE";
        }
        else {
            browsername="N/A";
        }
    }

    vendor=navigator.vendor;
    if (vendor!==null) {
        if (vendor.indexOf("Apple")!==-1) {
            browsername="Safari";
        }
        if (vendor.indexOf("Google")!==-1) {
            browsername="Chrome";
        }
    }

    browserversion="0";
    if (navigator.appVersion.indexOf("2.")!==-1) {
        browserversion="2";
    }
    if (navigator.appVersion.indexOf("3.")!==-1) {
        browserversion="3";
    }
    if (navigator.appVersion.indexOf("4.")!==-1) {
        browserversion="4";
    }
    if (navigator.appVersion.indexOf("5.")!==-1) {
        browserversion="5";
    }
    if (navigator.appVersion.indexOf("6.")!==-1) {
        browserversion="6";
    }

    if (browsername==="NS"||browsername==="Safari"||browsername==="Chrome") {
        document.getElementById('zone').innerHTML=ff;
    }
    if (browsername==="MSIE"){
        if (browserversion<=7){
            document.getElementById('zone').innerHTML=ie;
        }
        else {
            document.getElementById('zone').innerHTML=ff;
        }
    }
    if (browsername==="N/A") {
        document.getElementById('zone').innerHTML=ff;
    }
}
