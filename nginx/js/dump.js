function showProperties(id, x, y, sub) {
    var popover = $('#popover');

    if(lastHoverId){
        chart.setProperties({id: lastHoverId, c: null});
    }

    if(id) {

        ifShowing = true;

        var item = chart.getItem(id);

        console.log(item);

        var template,
            templateContent,
            html;

        if(isNode(item)) {
            templateContent = {
                objectID: item.objectID,
                start: item.start,
                name: item.name,
                strand: item.strand,
                end: item.end,
                chr: item.chr,
                tumor_type: item.tumor_type,
                version: item.version
            }

            template = $("#popoverNodeTemplate").html();

            html = Mustache.to_html(template, templateContent);

            $('popoverTitle').text("Bioentity");

        } else {

        }

        $('#content').html(html);

        var offset = $('#kl').offset();

        offset.left = x + 5;
        offset.top  = y - 75;

        popover.css('left', offset.left).css('top', offset.top);

        // a small delay looks better usually
        setTimeout(showPopover, 150);
    }
}