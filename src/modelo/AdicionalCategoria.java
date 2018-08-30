/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

/**

 @author jvbor
 */
public class AdicionalCategoria extends AdicionalProduto {

    private Categoria categoria;

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
         if (this.categoria != null && this.categoria != categoria) {
            this.categoria.getAdicionaisCategoria().remove(this);
        }
        if (categoria != this.categoria) {
            categoria.getAdicionaisCategoria().add(this);
        }
        this.categoria = categoria;
    }

}
