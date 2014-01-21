<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <title>Converter</title>
    <style>

body { font-family: sans-serif; font-size: 13px; }
a { text-decoration: none; color: #900; }
h1 { font-size: 16px; }
h2 { font-size: 14px; }
table.form { margin-left: 15px; }
td.label { text-align: right; right-padding: 10px; }

    </style>
    <script type="text/javascript" src="documentFormats.js"></script>
    <script type="text/javascript">

function updateOutputFormats(inputDocument) {

}

function doSubmit(form) {
	form.action = 'converted/document.'+ form.outputFormat.value;
	return true;
}

    </script>
  </head>

      <h2>HTML Preview Demonstration</h2>

      <form method="post" enctype="multipart/form-data" action="converted/document.pdf" onsubmit="return doSubmit(this)">
        <table class="form">
          <tr>
            <td class="label">Document:</td>
            <td>
              <input type="file" name="inputDocument" size="40" onchange="updateOutputFormats(this)"/>
            </td>
          </tr>
          <tr>
            <td class="label"></td>
            <td>
              <input  name="outputFormat" type="hidden" value="pdf"/>
              <input type="submit" value="Go!"/>
            </td>
          </tr>
        </table>
      </form>
      
  </body>
</html>
