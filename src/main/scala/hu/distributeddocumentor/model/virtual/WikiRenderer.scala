package hu.distributeddocumentor.model.virtual

trait WikiRenderer {
    def render(item: WikiItem): String
}
