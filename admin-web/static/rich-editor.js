(function () {
  function csrfToken() {
    const match = document.cookie.match(/(?:^|; )csrftoken=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : "";
  }

  function insertUpload(editor) {
    const input = document.createElement("input");
    input.type = "file";
    input.accept = "image/png,image/jpeg,image/webp,image/gif";
    input.onchange = async function () {
      const file = input.files && input.files[0];
      if (!file) return;
      const data = new FormData();
      data.append("file", file);
      try {
        const response = await fetch("/content/editor/upload-image/", {
          method: "POST",
          headers: {"X-CSRFToken": csrfToken()},
          body: data
        });
        const payload = await response.json();
        if (!response.ok || !payload.success) throw new Error(payload.message || "Upload gagal");
        editor.s.insertHTML('<p><img src="' + payload.url + '" alt="" style="max-width:100%;height:auto"></p>');
      } catch (err) {
        window.alert(err.message || "Upload gambar gagal");
      }
    };
    input.click();
  }

  function insertLatex(editor, display) {
    const example = display ? "\\frac{a}{b}" : "x^2 + y^2";
    const tex = window.prompt("Masukkan sintaks LaTeX", example);
    if (!tex) return;
    const text = display ? "\\[" + tex + "\\]" : "\\(" + tex + "\\)";
    editor.s.insertHTML(display ? "<p>" + text + "</p>" : text);
  }


  function boot() {
    if (!window.Jodit) return;
    document.querySelectorAll("textarea.cpns-rich-editor").forEach(function (textarea) {
      if (textarea.dataset.cpnsInitialized) return;
      textarea.dataset.cpnsInitialized = "1";
      const editor = window.Jodit.make(textarea, {
        minHeight: textarea.dataset.editorRole === "option" ? 150 : 230,
        toolbarAdaptive: false,
        spellcheck: true,
        defaultMode: window.Jodit.MODE_WYSIWYG,
        buttons: [
          "undo", "redo", "|", "bold", "italic", "underline", "strikethrough", "|",
          "paragraph", "fontsize", "brush", "|", "ul", "ol", "outdent", "indent", "|",
          "left", "center", "right", "justify", "|", "link", "image", "table", "hr", "|",
          "superscript", "subscript", "eraser", "|", "source", "fullsize"
        ],
        extraButtons: [
          {name: "cpnsUpload", text: "Upload gambar", tooltip: "Upload gambar dari komputer", exec: function (e) { insertUpload(e); }},
          {name: "cpnsLatex", text: "LaTeX", tooltip: "Sisipkan rumus inline", exec: function (e) { insertLatex(e, false); }},
          {name: "cpnsLatexBlock", text: "LaTeX blok", tooltip: "Sisipkan rumus blok", exec: function (e) { insertLatex(e, true); }}
        ],
        askBeforePasteHTML: false,
        askBeforePasteFromWord: false,
        cleanHTML: {fillEmptyParagraph: false},
        style: {fontFamily: "Inter, system-ui, sans-serif", fontSize: "15px"}
      });
    });
  }
  document.addEventListener("DOMContentLoaded", boot);
  if (document.readyState !== "loading") boot();
})();
