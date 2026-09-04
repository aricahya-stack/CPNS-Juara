from django.contrib import admin
from django.utils.html import format_html
from django.utils.safestring import mark_safe

from .content_utils import render_latex_to_mathml, sanitize_rich_html
from .forms import OptionInlineForm, QuestionAdminForm

from .models import (
    AppConfig,
    AppUser,
    Blueprint,
    Category,
    ImportBatch,
    ImportRow,
    Option,
    ProgressResult,
    Question,
    QuestionPackage,
    Subscription,
    Tryout,
)


class OptionInline(admin.StackedInline):
    model = Option
    extra = 5
    max_num = 5
    min_num = 5
    form = OptionInlineForm
    fields = ("code", "content_html", "score", "is_correct")


@admin.action(description="Tandai READY (belum tampil di APK)")
def mark_ready(modeladmin, request, queryset):
    queryset.update(status=Question.STATUS_READY, published=False)


@admin.action(description="PUBLISH ke paket/APK")
def publish_questions(modeladmin, request, queryset):
    queryset.update(status=Question.STATUS_PUBLISHED, published=True)


@admin.action(description="ARCHIVE")
def archive_questions(modeladmin, request, queryset):
    queryset.update(status=Question.STATUS_ARCHIVED, published=False)


@admin.register(Question)
class QuestionAdmin(admin.ModelAdmin):
    form = QuestionAdminForm
    list_display = (
        "code",
        "category",
        "blueprint",
        "stimulus_badge",
        "difficulty",
        "scoring_mode",
        "status",
        "published",
        "updated_at",
    )
    list_filter = (
        "category",
        "blueprint",
        "stimulus_kind",
        "difficulty",
        "scoring_mode",
        "status",
        "published",
    )
    search_fields = (
        "code",
        "prompt",
        "stimulus_text",
        "explanation",
        "subcategory",
        "blueprint__code",
        "blueprint__material",
    )
    autocomplete_fields = ("blueprint",)
    readonly_fields = ("stimulus_preview", "updated_at")
    inlines = [OptionInline]
    actions = [mark_ready, publish_questions, archive_questions]
    save_on_top = True
    fieldsets = (
        (
            "Identitas & Kisi-kisi",
            {
                "fields": (
                    ("code", "blueprint"),
                    ("category", "subcategory"),
                    ("difficulty", "scoring_mode"),
                    ("status", "published"),
                )
            },
        ),
        (
            "Stimulus Soal (opsional)",
            {
                "description": (
                    "Stimulus adalah bagian dari soal ini. Gunakan untuk cerita, bacaan, perintah, "
                    "gambar, grafik, diagram, atau kombinasi teks dan visual."
                ),
                "fields": (
                    "stimulus_kind",
                    "stimulus_text",
                    ("stimulus_image", "stimulus_image_url"),
                    "stimulus_preview",
                    "stimulus_source",
                ),
            },
        ),
        (
            "Pokok Soal & Pembahasan",
            {
                "fields": ("prompt", "explanation"),
            },
        ),
        (
            "Informasi Sistem",
            {
                "classes": ("collapse",),
                "fields": ("updated_at",),
            },
        ),
    )

    class Media:
        css = {
            "all": (
                "https://cdn.jsdelivr.net/npm/jodit@4.13.23/es2021/jodit.min.css",
                "rich-editor.css",
            )
        }
        js = (
            "https://cdn.jsdelivr.net/npm/jodit@4.13.23/es2021/jodit.min.js",
            "rich-editor.js",
        )

    @admin.display(description="Stimulus")
    def stimulus_badge(self, obj):
        if obj.stimulus_kind == Question.STIMULUS_NONE:
            return format_html('<span class="status-pill muted"><i class="bi bi-dash-circle"></i> Tidak</span>')
        icon_map = {
            Question.STIMULUS_TEXT: "bi-card-text",
            Question.STIMULUS_IMAGE: "bi-image",
            Question.STIMULUS_GRAPHIC: "bi-bar-chart-line",
            Question.STIMULUS_MIXED: "bi-layers",
        }
        label = dict(Question.STIMULUS_CHOICES).get(obj.stimulus_kind, obj.stimulus_kind)
        return format_html(
            '<span class="status-pill"><i class="bi {}"></i> {}</span>',
            icon_map.get(obj.stimulus_kind, "bi-paperclip"),
            label,
        )

    @admin.display(description="Preview stimulus")
    def stimulus_preview(self, obj):
        if not obj or not obj.pk:
            return "Simpan soal untuk melihat preview stimulus."

        text_html = ""
        if obj.stimulus_text:
            text_html = format_html(
                '<div class="stimulus-preview-text"><strong>Preview</strong><div class="rich-rendered">{}</div></div>',
                mark_safe(render_latex_to_mathml(sanitize_rich_html(obj.stimulus_text))),
            )

        image_url = ""
        if obj.stimulus_image:
            try:
                image_url = obj.stimulus_image.url
            except Exception:
                image_url = ""
        if not image_url:
            image_url = obj.stimulus_image_url or ""

        image_html = ""
        if image_url:
            image_html = format_html(
                '<div class="stimulus-preview-image"><img src="{}" alt="Preview stimulus"></div>',
                image_url,
            )

        if not text_html and not image_html:
            return format_html('<span class="help">Belum ada isi stimulus.</span>')
        return format_html('<div class="stimulus-preview">{}{}</div>', text_html, image_html)


@admin.register(Blueprint)
class BlueprintAdmin(admin.ModelAdmin):
    list_display = (
        "code",
        "category",
        "subtest",
        "material",
        "difficulty",
        "target_questions",
        "question_total",
        "active",
    )
    list_filter = ("category", "difficulty", "question_mode", "active")
    search_fields = ("code", "subtest", "material", "submaterial", "indicator")

    @admin.display(description="Jumlah Soal")
    def question_total(self, obj):
        return obj.questions.count()


@admin.register(ImportBatch)
class ImportBatchAdmin(admin.ModelAdmin):
    list_display = (
        "filename",
        "status",
        "total_rows",
        "valid_rows",
        "warning_rows",
        "error_rows",
        "imported_questions",
        "created_at",
    )
    list_filter = ("status", "created_at")
    readonly_fields = (
        "filename",
        "status",
        "created_by",
        "created_at",
        "confirmed_at",
        "total_rows",
        "valid_rows",
        "warning_rows",
        "error_rows",
        "imported_blueprints",
        "imported_questions",
    )


@admin.register(ImportRow)
class ImportRowAdmin(admin.ModelAdmin):
    list_display = ("batch", "sheet", "row_number", "entity_code", "status")
    list_filter = ("sheet", "status")
    search_fields = ("entity_code", "batch__filename")
    readonly_fields = (
        "batch",
        "sheet",
        "row_number",
        "entity_type",
        "entity_code",
        "status",
        "payload",
        "messages",
    )


@admin.register(Category)
class CategoryAdmin(admin.ModelAdmin):
    list_display = ("code", "name")
    search_fields = ("code", "name")


@admin.register(QuestionPackage)
class PackageAdmin(admin.ModelAdmin):
    list_display = ("name", "version", "active", "is_premium", "updated_at")
    list_filter = ("active", "is_premium")
    filter_horizontal = ("questions",)


@admin.register(Tryout)
class TryoutAdmin(admin.ModelAdmin):
    list_display = ("name", "duration_minutes", "active", "starts_at", "ends_at")
    filter_horizontal = ("questions",)


@admin.register(AppConfig)
class AppConfigAdmin(admin.ModelAdmin):
    list_display = ("key", "value", "description")
    search_fields = ("key", "description")


@admin.register(AppUser)
class AppUserAdmin(admin.ModelAdmin):
    list_display = ("display_name", "email", "is_premium", "created_at", "last_seen_at")
    list_filter = ("is_premium",)
    search_fields = ("display_name", "email", "firebase_uid")
    readonly_fields = ("firebase_uid", "created_at", "last_seen_at")


@admin.register(Subscription)
class SubscriptionAdmin(admin.ModelAdmin):
    list_display = ("user", "product_id", "status", "expiry_time", "auto_renew_enabled", "verified_at")
    list_filter = ("status", "product_id", "auto_renew_enabled")
    search_fields = ("user__email", "user__display_name", "product_id")
    readonly_fields = ("purchase_token_hash", "raw_snapshot", "verified_at", "created_at")
    exclude = ("purchase_token",)


@admin.register(ProgressResult)
class ProgressResultAdmin(admin.ModelAdmin):
    list_display = ("user", "category", "question_id", "score", "is_correct", "answered_at")
    list_filter = ("category", "is_correct")
    search_fields = ("user__email", "user__display_name")


admin.site.site_header = "CPNS JUARA Admin"
admin.site.site_title = "CPNS JUARA"
admin.site.index_title = "Kelola kisi-kisi, bank soal, paket, dan pengguna"
